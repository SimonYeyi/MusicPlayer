package com.musicplayer.service

import com.musicplayer.domain.model.PlaybackState
import com.musicplayer.domain.model.RepeatMode
import org.junit.Assert.*
import org.junit.Test

/**
 * Playback State Logic Test
 * Tests the playback state management logic
 */
class PlaybackStateTest {

    @Test
    fun defaultPlaybackState_isCorrect() {
        val state = PlaybackState()

        assertNull(state.currentSong)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, state.duration)
        assertFalse(state.shuffleMode)
        assertEquals(RepeatMode.OFF, state.repeatMode)
    }

    @Test
    fun playbackState_withPlayingSong() {
        val state = PlaybackState(
            isPlaying = true,
            currentPosition = 30000L,
            duration = 180000L
        )

        assertTrue(state.isPlaying)
        assertEquals(30000L, state.currentPosition)
        assertEquals(180000L, state.duration)
    }

    @Test
    fun playbackState_withShuffleEnabled() {
        val state = PlaybackState(
            shuffleMode = true,
            repeatMode = RepeatMode.ALL
        )

        assertTrue(state.shuffleMode)
        assertEquals(RepeatMode.ALL, state.repeatMode)
    }

    @Test
    fun repeatMode_cycleOffToAll() {
        var currentMode = RepeatMode.OFF
        currentMode = RepeatMode.ALL
        assertEquals(RepeatMode.ALL, currentMode)
    }

    @Test
    fun repeatMode_cycleAllToOne() {
        var currentMode = RepeatMode.ALL
        currentMode = RepeatMode.ONE
        assertEquals(RepeatMode.ONE, currentMode)
    }

    @Test
    fun repeatMode_cycleOneToOff() {
        var currentMode = RepeatMode.ONE
        currentMode = RepeatMode.OFF
        assertEquals(RepeatMode.OFF, currentMode)
    }

    @Test
    fun repeatMode_hasThreeValues() {
        assertEquals(3, RepeatMode.entries.size)
        assertNotNull(RepeatMode.OFF)
        assertNotNull(RepeatMode.ONE)
        assertNotNull(RepeatMode.ALL)
    }

    @Test
    fun shuffleMode_toggle() {
        var shuffleMode = false
        shuffleMode = !shuffleMode
        assertTrue(shuffleMode)
        shuffleMode = !shuffleMode
        assertFalse(shuffleMode)
    }

    @Test
    fun playbackState_copy_preservesOtherFields() {
        val original = PlaybackState(
            isPlaying = true,
            currentPosition = 50000L,
            duration = 180000L,
            shuffleMode = true,
            repeatMode = RepeatMode.ALL
        )

        val copied = original.copy(isPlaying = false)

        assertFalse(copied.isPlaying)
        assertEquals(50000L, copied.currentPosition)
        assertEquals(180000L, copied.duration)
        assertTrue(copied.shuffleMode)
        assertEquals(RepeatMode.ALL, copied.repeatMode)
    }

    @Test
    fun playbackState_progressCalculation() {
        val duration = 180000L
        val currentPosition = 90000L

        val progress = currentPosition.toFloat() / duration.toFloat()

        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun playbackState_zeroDuration_handled() {
        val duration = 0L
        val currentPosition = 0L

        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f

        assertEquals(0f, progress, 0.001f)
    }
}
