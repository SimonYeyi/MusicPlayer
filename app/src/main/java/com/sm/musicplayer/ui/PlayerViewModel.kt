package com.sm.musicplayer.ui

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sm.musicplayer.domain.model.PlayerState
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.domain.repository.PlaylistRepository
import com.sm.musicplayer.domain.repository.SongRepository
import com.sm.musicplayer.domain.usecase.CreatePlaylistUseCase
import com.sm.musicplayer.domain.usecase.GetAllPlaylistsUseCase
import com.sm.musicplayer.domain.usecase.GetAllSongsUseCase
import com.sm.musicplayer.domain.usecase.GetFavoriteSongsUseCase
import com.sm.musicplayer.domain.usecase.GetPopularSongsUseCase
import com.sm.musicplayer.domain.usecase.ScanLocalMusicUseCase
import com.sm.musicplayer.domain.usecase.ToggleFavoriteUseCase
import com.sm.musicplayer.domain.usecase.TogglePopularUseCase
import com.sm.musicplayer.service.PlayerController
import com.sm.musicplayer.ui.theme.MoodTheme
import com.sm.musicplayer.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackViewModel: PlaybackViewModel,
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val getPopularSongsUseCase: GetPopularSongsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val togglePopularUseCase: TogglePopularUseCase,
    private val scanLocalMusicUseCase: ScanLocalMusicUseCase,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = playbackViewModel.playerState

    val songs: StateFlow<List<Song>> = getAllSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = getFavoriteSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val popularSongs: StateFlow<List<Song>> = getPopularSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentSong: StateFlow<Song?> = playbackViewModel.currentSong
    val isPlaying: StateFlow<Boolean> = playbackViewModel.isPlaying
    val currentPosition: StateFlow<Long> = playbackViewModel.currentPosition
    val duration: StateFlow<Long> = playbackViewModel.duration

    val currentMoodTheme: StateFlow<MoodTheme> = themeManager.currentMoodTheme
        .stateIn(viewModelScope, SharingStarted.Lazily, MoodTheme.PASSION_RED)

    val isDarkMode: StateFlow<Boolean> = themeManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun playSong(song: Song) {
        val songList = songs.value
        val index = songList.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playbackViewModel.playSongs(songList, index)
        }
    }

    fun playSongs(songList: List<Song>, startIndex: Int = 0) {
        playbackViewModel.playSongs(songList, startIndex)
    }

    fun playPause() {
        playbackViewModel.playPause()
    }

    fun skipToNext() {
        playbackViewModel.skipToNext()
    }

    fun skipToPrevious() {
        playbackViewModel.skipToPrevious()
    }

    fun seekTo(position: Long) {
        playbackViewModel.seekTo(position)
    }

    fun seekToPercent(percent: Float) {
        playbackViewModel.seekToPercent(percent)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            toggleFavoriteUseCase(song.id, !song.isFavorite)
        }
    }

    fun togglePopular(song: Song) {
        viewModelScope.launch {
            togglePopularUseCase(song.id, !song.isPopular)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            createPlaylistUseCase(name)
        }
    }

    fun scanMusic(contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            scanLocalMusicUseCase(contentResolver)
            _uiState.value = _uiState.value.copy(isScanning = false)
        }
    }

    fun showMiniPlayer() {
        _uiState.value = _uiState.value.copy(showMiniPlayer = true, showFullPlayer = false)
    }

    fun showFullPlayer() {
        _uiState.value = _uiState.value.copy(showFullPlayer = true)
    }

    fun hideFullPlayer() {
        _uiState.value = _uiState.value.copy(showFullPlayer = false)
    }

    fun setMoodTheme(theme: MoodTheme) {
        viewModelScope.launch {
            themeManager.setMoodTheme(theme)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDark)
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        _isSearching.value = true
        viewModelScope.launch {
            val results = songs.value.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
            }
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
        }
    }

    fun getPlaylistWithSongs(playlistId: Long): Flow<Playlist?> {
        return playlistRepository.getPlaylistWithSongs(playlistId)
    }

    fun setSelectedPlaylist(playlist: Playlist?) {
        _uiState.value = _uiState.value.copy(selectedPlaylist = playlist)
    }

    fun setShowAddToPlaylistDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddToPlaylistDialog = show)
    }

    fun setSelectedSongForPlaylist(song: Song?) {
        _uiState.value = _uiState.value.copy(selectedSongForPlaylist = song)
    }

    fun setShowSearch(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSearch = show)
    }

    fun setShowPlaylistDetail(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPlaylistDetail = show)
    }

    fun setShowSettings(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSettings = show)
    }

    fun setCurrentTab(tab: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun dismissAddToPlaylistDialog() {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = false,
            selectedSongForPlaylist = null
        )
    }
}

data class MusicPlayerUiState(
    val isScanning: Boolean = false,
    val showMiniPlayer: Boolean = false,
    val showFullPlayer: Boolean = false,
    val showSettings: Boolean = false,
    val currentTab: Int = 0,
    val selectedPlaylist: Playlist? = null,
    val showAddToPlaylistDialog: Boolean = false,
    val selectedSongForPlaylist: Song? = null,
    val showSearch: Boolean = false,
    val showPlaylistDetail: Boolean = false
)
