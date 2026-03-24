package com.musicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.musicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.musicplayer.ACTION_PAUSE"
        const val ACTION_NEXT = "com.musicplayer.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.musicplayer.ACTION_PREVIOUS"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlaybackService = this@MusicPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            setupMediaSession()
        } catch (e: Exception) {
            // Log error but don't crash
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
        }
        // Return STICKY to keep service running, but don't start foreground yet
        return START_STICKY
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

    fun setPlaylist(songs: List<Song>, startSong: Song? = null, quiet: Boolean = false) {
        _currentPlaylist.value = songs
        if (startSong != null) {
            val index = songs.indexOfFirst { it.id == startSong.id }
            currentIndex = if (index >= 0) index else 0
        }
        isQuietMode = quiet
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
                return // Skip notification if permission not granted
            }
        }

        val song = _playbackState.value.currentSong ?: return

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (_playbackState.value.isPlaying) {
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

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
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
            .setOngoing(_playbackState.value.isPlaying)
            .build()

        startForeground(NOTIFICATION_ID, notification)
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
        serviceScope.cancel()
        releaseMediaPlayer()
        mediaSession?.release()
    }
}
