package com.sm.musicplayer.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songCount: Int = 0,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
