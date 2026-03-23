package com.sm.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sm.musicplayer.domain.model.PlayerState
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.service.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

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

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        playerController.playSongs(songs, startIndex)
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

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
