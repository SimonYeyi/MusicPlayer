package com.sm.musicplayer.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStateTest {

    @Test
    fun `progress calculates correctly when duration is positive`() {
        val playerState = PlayerState(
            currentSong = null,
            isPlaying = true,
            currentPosition = 30000L,
            duration = 60000L,
            playlist = emptyList(),
            currentIndex = 0
        )
        
        assertEquals(0.5f, playerState.progress, 0.001f)
    }

    @Test
    fun `progress returns zero when duration is zero`() {
        val playerState = PlayerState(
            currentSong = null,
            isPlaying = true,
            currentPosition = 30000L,
            duration = 0L,
            playlist = emptyList(),
            currentIndex = 0
        )
        
        assertEquals(0f, playerState.progress, 0.001f)
    }

    @Test
    fun `progress returns zero when duration is negative`() {
        val playerState = PlayerState(
            currentSong = null,
            isPlaying = true,
            currentPosition = 30000L,
            duration = -1L,
            playlist = emptyList(),
            currentIndex = 0
        )
        
        assertEquals(0f, playerState.progress, 0.001f)
    }

    @Test
    fun `PlayerState default values are correct`() {
        val playerState = PlayerState()
        
        assertEquals(null, playerState.currentSong)
        assertEquals(false, playerState.isPlaying)
        assertEquals(0L, playerState.currentPosition)
        assertEquals(0L, playerState.duration)
        assertEquals(emptyList(), playerState.playlist)
        assertEquals(-1, playerState.currentIndex)
    }
}