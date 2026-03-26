package com.musicplayer.service

import android.content.SharedPreferences
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.musicplayer.R
import com.musicplayer.domain.model.PlayMode
import com.musicplayer.domain.model.PlaybackState
import com.musicplayer.domain.model.Song
import com.musicplayer.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import com.musicplayer.data.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class MusicPlaybackService : Service() {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSessionCompat? = null

    @Inject
    lateinit var musicRepository: MusicRepository

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()

    private var currentIndex = 0
    private var isQuietMode = false
    private var playMode = PlayMode.LIST_LOOP

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    // 当前播放列表的标识，用于判断列表数据变化是否需要同步到播放队列
    private var currentPlaylistId: String? = null

    // 标志位：记录断连前是否在播放，用于重连后恢复（持久化到 SharedPreferences 以便 Service 被杀死后重建时能恢复）
    private var wasPlayingBeforeDeviceRemoval = false
    private lateinit var prefs: SharedPreferences

    // 当前专辑封面 bitmap
    private var currentAlbumArt: Bitmap? = null

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.musicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.musicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.musicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.musicplayer.ACTION_PREVIOUS"
        private const val PREFS_NAME = "music_playback_prefs"
        private const val KEY_WAS_PLAYING = "was_playing_before_removal"
        private const val KEY_PLAY_MODE = "play_mode"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService

        /** 接收 ViewModel 传过来的排序后本地音乐 Flow，保持播放队列与本地音乐界面完全一致的排序 */
        fun setSortedLocalMusicFlow(flow: Flow<List<Song>>) {
            this@MusicPlaybackService.setSortedLocalMusicFlow(flow)
        }
    }

    /** 观察 ViewModel 传来的排序后本地音乐列表，仅当当前播放来源为本地音乐时更新队列 */
    fun setSortedLocalMusicFlow(flow: Flow<List<Song>>) {
        serviceScope.launch {
            flow.collect { songs ->
                // 进程启动后，队列为空时用排序后的本地音乐初始化
                if (_currentPlaylist.value.isEmpty() && songs.isNotEmpty()) {
                    currentPlaylistId = "local"
                    _currentPlaylist.value = songs
                    val firstSong = songs.first()
                    _playbackState.value = _playbackState.value.copy(currentSong = firstSong)
                    loadAlbumArt(firstSong.uri)
                } else if (currentPlaylistId == "local") {
                    _currentPlaylist.value = songs
                }
            }
        }
    }

    // 音频设备回调：设备断开时暂停、重新连接时继续播放（API 31+）
    private inner class AudioDeviceCallbackImpl : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(devices: Array<out AudioDeviceInfo>) {
            val isPlaying = _playbackState.value.isPlaying
            if (isPlaying) {
                wasPlayingBeforeDeviceRemoval = true
                prefs.edit().putBoolean(KEY_WAS_PLAYING, true).apply()
                pause()
            }
        }

        override fun onAudioDevicesAdded(devices: Array<out AudioDeviceInfo>) {
            if (wasPlayingBeforeDeviceRemoval) {
                wasPlayingBeforeDeviceRemoval = false
                prefs.edit().putBoolean(KEY_WAS_PLAYING, false).apply()
                play()
            }
        }
    }

    // 音频断开广播：设备断开时暂停播放（API < 31）
    private val audioNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                if (_playbackState.value.isPlaying) {
                    wasPlayingBeforeDeviceRemoval = true
                    prefs.edit().putBoolean(KEY_WAS_PLAYING, true).apply()
                    pause()
                }
            }
        }
    }

    private var audioDeviceCallback: AudioDeviceCallbackImpl? = null

    override fun onCreate() {
        super.onCreate()
        try {
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            // 恢复播放模式
            val savedPlayMode = prefs.getString(KEY_PLAY_MODE, null)
            if (savedPlayMode != null) {
                playMode = try { PlayMode.valueOf(savedPlayMode) } catch (e: Exception) { PlayMode.LIST_LOOP }
                _playbackState.value = _playbackState.value.copy(playMode = playMode)
            }
            createNotificationChannel()
            setupMediaSession()
            registerAudioCallbacks()
            // 检查是否需要恢复因蓝牙断连而暂停的播放（Service 重建时读取 SharedPreferences）
            if (prefs.getBoolean(KEY_WAS_PLAYING, false)) {
                wasPlayingBeforeDeviceRemoval = true
                prefs.edit().putBoolean(KEY_WAS_PLAYING, false).apply()
                // 延迟一小段时间确保 MediaPlayer 已准备好
                serviceScope.launch {
                    delay(200)
                    if (wasPlayingBeforeDeviceRemoval) {
                        wasPlayingBeforeDeviceRemoval = false
                        play()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 立即启动前台服务，确保 Service 在后台持续运行
        startForeground(NOTIFICATION_ID, createForegroundNotification())

        // 检查是否需要恢复因蓝牙断连而暂停的播放（覆盖 onCreate 中可能遗漏的恢复）
        if (prefs.getBoolean(KEY_WAS_PLAYING, false)) {
            wasPlayingBeforeDeviceRemoval = true
            prefs.edit().putBoolean(KEY_WAS_PLAYING, false).apply()
            serviceScope.launch {
                delay(200)
                if (wasPlayingBeforeDeviceRemoval) {
                    wasPlayingBeforeDeviceRemoval = false
                    play()
                }
            }
        }

        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        return START_STICKY
    }

    private fun createForegroundNotification(): android.app.Notification {
        val state = _playbackState.value
        val song = state.currentSong

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (state.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_launcher_foreground,
                getString(R.string.pause),
                createPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_launcher_foreground,
                getString(R.string.play),
                createPendingIntent(ACTION_PLAY)
            )
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song?.title ?: getString(R.string.app_name))
            .setContentText(song?.artist ?: "")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.previous),
                createPendingIntent(ACTION_PREVIOUS)
            )
            .addAction(playPauseAction)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.next),
                createPendingIntent(ACTION_NEXT)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
            )
            .setOngoing(state.isPlaying)

        // 设置专辑封面，无封面时使用默认图
        if (currentAlbumArt != null) {
            builder.setLargeIcon(currentAlbumArt)
        } else {
            val defaultBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_default_album)
            builder.setLargeIcon(defaultBitmap)
        }

        return builder.build()
    }

    private fun registerAudioCallbacks() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioDeviceCallback = AudioDeviceCallbackImpl()
            audioManager.registerAudioDeviceCallback(audioDeviceCallback!!, null)
        } else {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            registerReceiver(audioNoisyReceiver, filter)
        }
    }

    private fun unregisterAudioCallbacks() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioDeviceCallback?.let { audioManager.unregisterAudioDeviceCallback(it) }
        } else {
            try {
                unregisterReceiver(audioNoisyReceiver)
            } catch (_: Exception) { }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaSession() {
        try {
            mediaSession = MediaSessionCompat(this, "MusicPlayerSession").apply {
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onPlay() {
                        play()
                    }

                    override fun onPause() {
                        pause()
                    }

                    override fun onSkipToNext() {
                        playNext()
                    }

                    override fun onSkipToPrevious() {
                        playPrevious()
                    }

                    override fun onSeekTo(pos: Long) {
                        seekTo(pos.toInt())
                    }

                    override fun onStop() {
                        stop()
                    }
                })
                isActive = true
            }
        } catch (e: Exception) {
            // Media session setup failed, continue without it
        }
    }

    fun setPlaylist(songs: List<Song>, startSong: Song? = null, quiet: Boolean = false, playlistId: String? = null) {
        _currentPlaylist.value = songs
        currentPlaylistId = playlistId
        if (startSong != null) {
            val index = songs.indexOfFirst { it.id == startSong.id }
            currentIndex = if (index >= 0) index else 0
        }
        isQuietMode = quiet
    }

    // 同步播放列表：只有列表标识与当前播放的列表匹配时才更新
    fun syncPlaylist(playlistId: String, songs: List<Song>) {
        if (currentPlaylistId != playlistId) return
        val currentSong = _playbackState.value.currentSong
        // 如果同步过来的歌曲列表为空，但播放器中有正在显示的歌曲，保留这首歌
        val finalSongs = if (songs.isEmpty() && currentSong != null) listOf(currentSong) else songs
        _currentPlaylist.value = finalSongs
        // 保持当前歌曲位置同步
        if (currentSong != null) {
            val index = finalSongs.indexOfFirst { it.id == currentSong.id }
            if (index >= 0) {
                currentIndex = index
            }
        }
        // 刷新通知栏的上一曲下一曲按钮
        updateNotification()
    }

    fun playSong(song: Song, quiet: Boolean = false) {
        val playlist = _currentPlaylist.value
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            currentIndex = index
        }
        prepareAndPlay(song, quiet)
    }

    fun playSongAtIndex(index: Int, quiet: Boolean = false) {
        if (index in _currentPlaylist.value.indices) {
            currentIndex = index
            playSong(_currentPlaylist.value[index], quiet)
        }
    }

    private fun prepareAndPlay(song: Song, quiet: Boolean = false) {
        releaseMediaPlayer()

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setDataSource(this@MusicPlaybackService, song.uri)

            setOnPreparedListener { mp ->
                mp.start()
                _playbackState.value = _playbackState.value.copy(
                    currentSong = song,
                    isPlaying = true,
                    duration = mp.duration.toLong()
                )
                // 加载专辑封面并更新通知（使用音频文件 URI，MediaMetadataRetriever 会提取嵌入的封面）
                loadAlbumArt(song.uri)
                // 添加到最近播放（静默模式不更新）
                if (!quiet) {
                    serviceScope.launch {
                        musicRepository.addRecentPlay(song.id)
                    }
                }
                startPositionUpdate()
                updateNotification()
                updateMediaSession()
            }

            setOnCompletionListener {
                when (playMode) {
                    PlayMode.ONE_LOOP -> {
                        seekTo(0)
                        play()
                    }
                    PlayMode.LIST_LOOP -> {
                        playNext()
                    }
                    PlayMode.SHUFFLE -> {
                        playNext()
                    }
                    PlayMode.OFF -> {
                        if (currentIndex < _currentPlaylist.value.size - 1) {
                            playNext()
                        } else {
                            _playbackState.value = _playbackState.value.copy(isPlaying = false)
                            stopPositionUpdate()
                        }
                    }
                }
            }

            setOnErrorListener { _, _, _ ->
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                true
            }

            prepareAsync()
        }
    }

    fun play() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startPositionUpdate()
                updateNotification()
                updateMediaSession()
            }
        } ?: run {
            _currentPlaylist.value.getOrNull(currentIndex)?.let { song ->
                playSong(song)
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                stopPositionUpdate()
                updateNotification()
                updateMediaSession()
            }
        }
    }

    fun playNext() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        currentIndex = if (playMode == PlayMode.SHUFFLE) {
            (playlist.indices).random()
        } else {
            (currentIndex + 1) % playlist.size
        }
        playSong(playlist[currentIndex], quiet = isQuietMode)
    }

    fun playPrevious() {
        val playlist = _currentPlaylist.value
        if (playlist.isEmpty()) return

        currentIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            playlist.size - 1
        }
        playSong(playlist[currentIndex], quiet = isQuietMode)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
        _playbackState.value = _playbackState.value.copy(currentPosition = position.toLong())
    }

    fun togglePlayMode() {
        playMode = when (playMode) {
            PlayMode.LIST_LOOP -> PlayMode.SHUFFLE
            PlayMode.SHUFFLE -> PlayMode.ONE_LOOP
            PlayMode.ONE_LOOP -> PlayMode.OFF
            PlayMode.OFF -> PlayMode.LIST_LOOP
        }
        _playbackState.value = _playbackState.value.copy(playMode = playMode)
        prefs.edit().putString(KEY_PLAY_MODE, playMode.name).apply()
    }

    fun stop() {
        mediaPlayer?.stop()
        _playbackState.value = PlaybackState()
        stopPositionUpdate()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun startPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = serviceScope.launch {
            while (isActive) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _playbackState.value = _playbackState.value.copy(
                            currentPosition = it.currentPosition.toLong()
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdate() {
        positionUpdateJob?.cancel()
    }

    private fun updateNotification() {
        // Check notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun loadAlbumArt(audioUri: Uri) {
        serviceScope.launch {
            try {
                val bitmap = extractAlbumArtFromAudio(audioUri)
                if (bitmap != null) {
                    // 缩放图片避免通知过大
                    val scaled = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
                    if (scaled != bitmap) bitmap.recycle()
                    currentAlbumArt = scaled
                } else {
                    currentAlbumArt = null
                }
            } catch (_: Exception) {
                currentAlbumArt = null
            }
            // 无论是否有封面，都更新通知以刷新封面显示状态
            updateNotification()
        }
    }

    private fun extractAlbumArtFromAudio(audioUri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.openFileDescriptor(audioUri, "r")?.use { pfd ->
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(pfd.fileDescriptor)
                    val art = retriever.embeddedPicture?.let { bytes ->
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    retriever.release()
                    art
                }
            } else {
                contentResolver.query(
                    audioUri,
                    arrayOf(MediaStore.MediaColumns.DATA),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        )
                        if (path != null) {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(path)
                            val art = retriever.embeddedPicture?.let { bytes ->
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            retriever.release()
                            art
                        } else null
                    } else null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun updateMediaSession() {
        val state = _playbackState.value
        val mediaPlaybackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (state.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                state.currentPosition,
                1f
            )
            .build()

        mediaSession?.setPlaybackState(mediaPlaybackState)

        state.currentSong?.let { song ->
            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
                .build()
            mediaSession?.setMetadata(metadata)
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterAudioCallbacks()
        serviceScope.cancel()
        releaseMediaPlayer()
        mediaSession?.release()
    }
}
