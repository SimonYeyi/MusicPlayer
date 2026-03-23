package com.sm.musicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sm.musicplayer.data.repository.LyricsRepository
import com.sm.musicplayer.domain.model.Lyrics
import com.sm.musicplayer.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasLyrics = MutableStateFlow(false)
    val hasLyrics: StateFlow<Boolean> = _hasLyrics.asStateFlow()

    fun loadLyrics(song: Song?) {
        if (song == null) {
            _lyrics.value = null
            _hasLyrics.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val loadedLyrics = lyricsRepository.getLyricsForSong(
                songPath = song.path,
                title = song.title,
                artist = song.artist
            )
            _lyrics.value = loadedLyrics
            _hasLyrics.value = loadedLyrics != null && loadedLyrics.lines.isNotEmpty()
            _isLoading.value = false
        }
    }

    fun getCurrentLineIndex(currentPosition: Long): Int {
        return _lyrics.value?.getCurrentLineIndex(currentPosition) ?: -1
    }
}
