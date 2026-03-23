package com.sm.musicplayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SongTest {

    @Test
    fun `durationFormatted returns correct format for minutes and seconds`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumId = 1L,
            duration = 185000L,
            path = "/path/to/song.mp3",
            dateAdded = System.currentTimeMillis()
        )
        
        assertEquals("3:05", song.durationFormatted)
    }

    @Test
    fun `durationFormatted returns 0 0 for zero duration`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumId = 1L,
            duration = 0L,
            path = "/path/to/song.mp3",
            dateAdded = System.currentTimeMillis()
        )
        
        assertEquals("0:00", song.durationFormatted)
    }

    @Test
    fun `durationFormatted returns correct format for long duration`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumId = 1L,
            duration = 3661000L,
            path = "/path/to/song.mp3",
            dateAdded = System.currentTimeMillis()
        )
        
        assertEquals("61:01", song.durationFormatted)
    }

    @Test
    fun `Song with default values has isFavorite and isPopular as false`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            albumId = 1L,
            duration = 180000L,
            path = "/path/to/song.mp3",
            dateAdded = System.currentTimeMillis()
        )
        
        assertEquals(false, song.isFavorite)
        assertEquals(false, song.isPopular)
    }
}