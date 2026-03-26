package com.musicplayer.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.data.local.PlaylistEntity
import com.musicplayer.data.repository.MusicRepository
import com.musicplayer.domain.model.MoodTheme
import com.musicplayer.domain.model.PlaybackState
import com.musicplayer.domain.model.Song
import com.musicplayer.service.MusicPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val playbackState: PlaybackState = PlaybackState(),
    val currentPlaylist: List<Song> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val currentTheme: MoodTheme = MoodTheme.CALM,
    val showThemePicker: Boolean = false,
    val playlists: List<PlaylistEntity> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet(),
    val recentSongIds: List<Long> = emptyList(),
    val pinnedArtists: Set<String> = emptySet(),
    val selectedMyMusicTab: Int = 0,
    val hasLoadedSongs: Boolean = false,
    val showPermissionDeniedDialog: Boolean = false,
    val permissionDeniedTitle: String = "",
    val permissionDeniedMessage: String = "",
    val showLyricsUnavailable: Boolean = false
)

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val application: Application,
    private val musicRepository: MusicRepository
) : AndroidViewModel(application) {

    // 删除文件请求状态，Activity 观察此状态并处理系统弹框
    private val _pendingDeleteRequest = MutableStateFlow<PendingDeleteRequest?>(null)
    val pendingDeleteRequest: StateFlow<PendingDeleteRequest?> = _pendingDeleteRequest.asStateFlow()

    data class PendingDeleteRequest(
        val songId: Long,
        val songUri: Uri,
        val isCurrentlyPlaying: Boolean
    )

    fun onDeleteFileConfirmed(songId: Long) {
        viewModelScope.launch {
            val wasPlaying = _pendingDeleteRequest.value?.isCurrentlyPlaying == true
            musicRepository.deleteSong(songId)
            _pendingDeleteRequest.value = null
            if (wasPlaying) {
                playNext()
            }
        }
    }

    fun onDeleteFileDismissed() {
        _pendingDeleteRequest.value = null
    }

    companion object {
        private const val PREFS_NAME = "music_player_prefs"
        private const val KEY_THEME = "current_theme"
        private const val KEY_PINNED_ARTISTS = "pinned_artists"
    }

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val savedTheme: MoodTheme
        get() {
            val themeName = sharedPreferences.getString(KEY_THEME, MoodTheme.CALM.name)
            return try {
                MoodTheme.valueOf(themeName ?: MoodTheme.CALM.name)
            } catch (e: IllegalArgumentException) {
                MoodTheme.CALM
            }
        }

    private val savedPinnedArtists: Set<String>
        get() {
            return sharedPreferences.getStringSet(KEY_PINNED_ARTISTS, emptySet()) ?: emptySet()
        }

    private val _uiState = MutableStateFlow(MusicUiState(currentTheme = savedTheme, pinnedArtists = savedPinnedArtists))
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var musicService: MusicPlaybackService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                val binder = service as MusicPlaybackService.MusicBinder
                musicService = binder.getService()
                serviceBound = true
                observePlaybackState()
            } catch (e: Exception) {
                serviceBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            serviceBound = false
        }
    }

    init {
        observeSongs()
        observePlaylists()
        observeFavorites()
        observeRecentPlays()
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun bindService() {
        try {
            val intent = Intent(application, MusicPlaybackService::class.java)
            application.startService(intent)
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            // Service binding failed
        }
    }

    private fun observeSongs() {
        viewModelScope.launch {
            musicRepository.getAllSongs().collect { songs ->
                _uiState.update { state ->
                    state.copy(
                        songs = songs,
                        filteredSongs = if (state.searchQuery.isEmpty()) songs
                        else songs.filter {
                            it.title.contains(state.searchQuery, ignoreCase = true) ||
                                    it.artist.contains(state.searchQuery, ignoreCase = true)
                        },
                        hasLoadedSongs = true
                    )
                }
                // 数据库歌曲变化（扫描/重排）后，重新计算本地音乐排序并同步到播放队列
                syncLocalMusicSortedPlaylist(_uiState.value.pinnedArtists)
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            musicRepository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
        // 监听所有歌单歌曲变化，同步到播放队列
        viewModelScope.launch {
            musicRepository.playlistSongsChanged.collect { (playlistId, songs) ->
                musicService?.syncPlaylist(playlistId.toString(), songs)
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            musicRepository.getFavoriteSongIds().collect { ids ->
                _uiState.update { it.copy(favoriteSongIds = ids.toSet()) }
                // 如果当前播放的是收藏列表，同步更新播放队列
                val favoriteSongs = _uiState.value.songs.filter { ids.contains(it.id) }
                musicService?.syncPlaylist("favorites", favoriteSongs)
            }
        }
    }

    private fun observeRecentPlays() {
        viewModelScope.launch {
            musicRepository.getRecentPlaySongIds().collect { ids ->
                _uiState.update { it.copy(recentSongIds = ids) }
                // 如果当前播放的是最近播放列表，同步更新播放队列
                val recentSongs = ids.mapNotNull { id -> _uiState.value.songs.find { it.id == id } }
                musicService?.syncPlaylist("recent", recentSongs)
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch {
            musicService?.playbackState?.collect { state: PlaybackState ->
                _uiState.update { it.copy(playbackState = state) }
            }
        }
        viewModelScope.launch {
            musicService?.currentPlaylist?.collect { playlist ->
                _uiState.update { it.copy(currentPlaylist = playlist) }
            }
        }
    }

    fun scanMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                musicRepository.scanMusicFromDevice()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshMusic() {
        scanMusic()
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasPermission = true) }
        if (!serviceBound) {
            bindService()
        }
        // 如果本地已有歌曲数据，跳过首次扫描
        viewModelScope.launch {
            if (!musicRepository.hasSongs()) {
                scanMusic()
            }
        }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(hasPermission = false) }
    }

    fun showPermissionDeniedDialog(title: String, message: String) {
        _uiState.update { it.copy(showPermissionDeniedDialog = true, permissionDeniedTitle = title, permissionDeniedMessage = message) }
    }

    fun dismissPermissionDeniedDialog() {
        _uiState.update { it.copy(showPermissionDeniedDialog = false) }
    }

    fun showLyricsUnavailable() {
        _uiState.update { it.copy(showLyricsUnavailable = true) }
    }

    fun dismissLyricsUnavailable() {
        _uiState.update { it.copy(showLyricsUnavailable = false) }
    }

    fun playSong(song: Song) {
        musicService?.let { service ->
            if (_uiState.value.songs.isNotEmpty()) {
                service.setPlaylist(_uiState.value.songs, playlistId = "local")
                // 如果已是当前歌曲且处于暂停状态，继续播放而非重新播放
                if (service.playbackState.value.currentSong?.id == song.id && !service.playbackState.value.isPlaying) {
                    service.play()
                } else {
                    service.playSong(song)
                }
            }
        }
    }

    fun playSongs(songs: List<Song>, startSong: Song, playlistId: String = "local") {
        musicService?.let { service ->
            if (songs.isNotEmpty()) {
                service.setPlaylist(songs, startSong, playlistId = playlistId)
                val currentSongId = service.playbackState.value.currentSong?.id
                val isPlaying = service.playbackState.value.isPlaying
                val isSameSong = currentSongId == startSong.id
                if (isSameSong) {
                    if (!isPlaying) {
                        service.play()
                    }
                } else {
                    service.playSong(startSong)
                }
            }
        }
    }

    fun playSongAtIndex(index: Int) {
        musicService?.playSongAtIndex(index)
    }

    // 播放歌曲列表但不更新最近播放记录（用于最近播放列表）
    fun playSongsQuietly(songs: List<Song>, startSong: Song) {
        musicService?.let { service ->
            if (songs.isNotEmpty()) {
                service.setPlaylist(songs, startSong, quiet = true, playlistId = "recent")
                val currentSongId = service.playbackState.value.currentSong?.id
                val isPlaying = service.playbackState.value.isPlaying
                val isSameSong = currentSongId == startSong.id
                if (isSameSong) {
                    if (!isPlaying) {
                        service.play()
                    }
                } else {
                    service.playSong(startSong, quiet = true)
                }
            }
        }
    }

    fun playPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val songIds = musicRepository.getSongIdsInPlaylistDirect(playlistId)
            val songs = musicRepository.getSongsByIds(songIds)
            if (songs.isNotEmpty()) {
                musicService?.setPlaylist(songs, playlistId = playlistId.toString())
                musicService?.playSong(songs.first())
            }
        }
    }

    fun togglePlayPause() {
        musicService?.let { service ->
            if (service.playbackState.value.isPlaying) {
                service.pause()
            } else {
                service.play()
            }
        }
    }

    fun playNext() {
        musicService?.playNext()
    }

    fun playPrevious() {
        musicService?.playPrevious()
    }

    fun seekTo(position: Long) {
        musicService?.seekTo(position.toInt())
    }

    fun toggleShuffle() {
        musicService?.togglePlayMode()
    }

    fun toggleRepeat() {
        musicService?.togglePlayMode()
    }

    fun setTheme(theme: MoodTheme) {
        sharedPreferences.edit().putString(KEY_THEME, theme.name).apply()
        _uiState.update { it.copy(currentTheme = theme) }
    }

    fun toggleThemePicker() {
        _uiState.update { it.copy(showThemePicker = !it.showThemePicker) }
    }

    fun hideThemePicker() {
        _uiState.update { it.copy(showThemePicker = false) }
    }

    fun search(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredSongs = if (query.isEmpty()) state.songs
                else state.songs.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.artist.contains(query, ignoreCase = true)
                }
            )
        }
    }

    // ===== 歌单管理 =====
    suspend fun createPlaylist(name: String): Long {
        return musicRepository.createPlaylist(name)
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            musicRepository.renamePlaylist(playlistId, newName)
        }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun moveSongToPlaylist(fromPlaylistId: Long, toPlaylistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(fromPlaylistId, songId)
            musicRepository.addSongToPlaylist(toPlaylistId, songId)
        }
    }

    fun moveSongsToPlaylist(fromPlaylistId: Long, toPlaylistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            songIds.forEach { songId ->
                musicRepository.removeSongFromPlaylist(fromPlaylistId, songId)
                musicRepository.addSongToPlaylist(toPlaylistId, songId)
            }
        }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun clearPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val songIds = musicRepository.getSongIdsInPlaylistDirect(playlistId)
            songIds.forEach { songId ->
                musicRepository.removeSongFromPlaylist(playlistId, songId)
            }
        }
    }

    suspend fun getPlaylistSongs(playlistId: Long): List<Song> {
        return musicRepository.getSongsForPlaylist(playlistId)
    }

    // ===== 收藏管理 =====
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(songId)
        }
    }

    fun isFavorite(songId: Long): Boolean {
        return _uiState.value.favoriteSongIds.contains(songId)
    }

    fun pinArtist(artist: String) {
        val currentPinned = _uiState.value.pinnedArtists.toMutableSet()
        currentPinned.add(artist)
        savePinnedArtists(currentPinned)
    }

    fun unpinArtist(artist: String) {
        val currentPinned = _uiState.value.pinnedArtists.toMutableSet()
        currentPinned.remove(artist)
        savePinnedArtists(currentPinned)
    }

    fun togglePinArtist(artist: String) {
        val currentPinned = _uiState.value.pinnedArtists
        if (currentPinned.contains(artist)) {
            unpinArtist(artist)
        } else {
            pinArtist(artist)
        }
    }

    fun setSelectedMyMusicTab(tab: Int) {
        _uiState.update { it.copy(selectedMyMusicTab = tab) }
    }

    private fun savePinnedArtists(artists: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_PINNED_ARTISTS, artists).apply()
        _uiState.update { it.copy(pinnedArtists = artists) }
        // 置顶艺术家变化时，重新计算排序并同步到播放队列
        syncLocalMusicSortedPlaylist(artists)
    }

    private fun syncLocalMusicSortedPlaylist(pinnedArtists: Set<String>) {
        val songs = _uiState.value.songs
        if (songs.isEmpty()) return
        val groupedSongs = songs.groupBy { it.artist }
        val pinnedArtistsList = pinnedArtists.filter { groupedSongs.containsKey(it) }.toSet()
        val unpinnedArtists = groupedSongs.keys.filter { !pinnedArtistsList.contains(it) }.sorted()
        val sortedSongs: List<Song> = buildList {
            pinnedArtistsList.forEach { artist -> groupedSongs[artist]?.let { addAll(it) } }
            unpinnedArtists.forEach { artist -> groupedSongs[artist]?.let { addAll(it) } }
        }
        musicService?.syncPlaylist("local", sortedSongs)
    }

    // ===== 最近播放 =====
    fun getRecentSongs(): List<Song> {
        val recentIds = _uiState.value.recentSongIds
        val allSongs = _uiState.value.songs
        return recentIds.mapNotNull { id -> allSongs.find { it.id == id } }
    }

    fun clearRecentPlays() {
        viewModelScope.launch {
            musicRepository.clearRecentPlays()
        }
    }

    fun removeFromRecentPlays(songId: Long) {
        viewModelScope.launch {
            musicRepository.removeFromRecentPlays(songId)
        }
    }

    // ===== 删除歌曲 =====
    fun deleteSong(songId: Long, deleteFile: Boolean) {
        val isCurrentlyPlaying = _uiState.value.playbackState.currentSong?.id == songId
        if (deleteFile) {
            viewModelScope.launch {
                val uri = musicRepository.getSongUri(songId)
                if (uri != null) {
                    _pendingDeleteRequest.value = PendingDeleteRequest(songId, uri, isCurrentlyPlaying)
                } else {
                    musicRepository.deleteSong(songId)
                    if (isCurrentlyPlaying) {
                        playNext()
                    }
                }
            }
        } else {
            viewModelScope.launch {
                musicRepository.deleteSong(songId)
                if (isCurrentlyPlaying) {
                    playNext()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (serviceBound) {
            application.unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
