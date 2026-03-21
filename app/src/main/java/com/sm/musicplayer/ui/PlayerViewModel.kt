package com.sm.musicplayer.ui

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sm.musicplayer.domain.model.PlayerState
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.domain.usecase.*
import com.sm.musicplayer.service.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val getFavoriteSongsUseCase: GetFavoriteSongsUseCase,
    private val getPopularSongsUseCase: GetPopularSongsUseCase,
    private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val togglePopularUseCase: TogglePopularUseCase,
    private val scanLocalMusicUseCase: ScanLocalMusicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicPlayerUiState())
    val uiState: StateFlow<MusicPlayerUiState> = _uiState.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    val songs: StateFlow<List<Song>> = getAllSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = getFavoriteSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val popularSongs: StateFlow<List<Song>> = getPopularSongsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<Playlist>> = getAllPlaylistsUseCase()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    init {
        playerController.initialize()
        startPositionUpdater()
        
        viewModelScope.launch {
            combine(currentSong, isPlaying) { song, playing ->
                song to playing
            }.collect { (song, playing) ->
                _playerState.value = _playerState.value.copy(
                    currentSong = song,
                    isPlaying = playing
                )
            }
        }
    }

    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (isActive) {
                if (playerController.isPlaying.value) {
                    _currentPosition.value = playerController.getCurrentPosition()
                    _duration.value = playerController.getDuration()
                }
                delay(200)
            }
        }
    }

    fun playSong(song: Song) {
        val songList = songs.value
        val index = songList.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playerController.playSongs(songList, index)
        }
    }

    fun playSongs(songList: List<Song>, startIndex: Int = 0) {
        playerController.playSongs(songList, startIndex)
    }

    fun playPause() {
        playerController.playPause()
    }

    fun skipToNext() {
        playerController.skipToNext()
    }

    fun skipToPrevious() {
        playerController.skipToPrevious()
    }

    fun seekTo(position: Long) {
        playerController.seekTo(position)
    }

    fun seekToPercent(percent: Float) {
        playerController.seekToPercent(percent)
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
}

data class MusicPlayerUiState(
    val isScanning: Boolean = false,
    val showMiniPlayer: Boolean = false,
    val showFullPlayer: Boolean = false,
    val currentTab: Int = 0
)
