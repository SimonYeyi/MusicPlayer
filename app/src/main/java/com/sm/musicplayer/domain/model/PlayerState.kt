package com.sm.musicplayer.domain.model

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
}
