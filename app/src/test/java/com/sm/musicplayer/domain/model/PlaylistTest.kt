package com.sm.musicplayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaylistTest {

    @Test
    fun `Playlist with default values has correct defaults`() {
        val playlist = Playlist(name = "Test Playlist")
        
        assertEquals(0L, playlist.id)
        assertEquals("Test Playlist", playlist.name)
        assertEquals(0, playlist.songCount)
        assertEquals(emptyList<Song>(), playlist.songs)
    }

    @Test
    fun `Playlist with songs has correct songCount`() {
        val songs = listOf(
            Song(1L, "Song 1", "Artist", "Album", 1L, 180000L, "/path/1", 0),
            Song(2L, "Song 2", "Artist", "Album", 1L, 200000L, "/path/2", 0),
            Song(3L, "Song 3", "Artist", "Album", 1L, 220000L, "/path/3", 0)
        )
        
        val playlist = Playlist(
            id = 1L,
            name = "My Playlist",
            songCount = songs.size,
            songs = songs,
            createdAt = System.currentTimeMillis()
        )
        
        assertEquals(3, playlist.songCount)
        assertEquals(3, playlist.songs.size)
    }

    @Test
    fun `Playlist createdAt defaults to current time`() {
        val beforeCreation = System.currentTimeMillis()
        val playlist = Playlist(name = "Test")
        val afterCreation = System.currentTimeMillis()
        
        assert(playlist.createdAt in beforeCreation..afterCreation)
    }
}