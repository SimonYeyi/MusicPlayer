package com.sm.musicplayer.domain.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val isPopular: Boolean = false
) {
    val durationFormatted: String
        get() {
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
