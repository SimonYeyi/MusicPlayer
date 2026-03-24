package com.musicplayer.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Domain 层模型单元测试
 * 注意：由于Song类使用Android Uri类，部分测试需要InstrumentedTest环境
 */
class DomainModelsTest {

    // ==================== PlaybackState Tests ====================

    @Test
    fun `PlaybackState 默认值正确`() {
        val state = PlaybackState()

        assertNull(state.currentSong)
        assertFalse(state.isPlaying)
        assertEquals(0L, state.currentPosition)
        assertEquals(0L, state.duration)
        assertFalse(state.shuffleMode)
        assertEquals(RepeatMode.OFF, state.repeatMode)
    }

    @Test
    fun `PlaybackState copy 正常工作`() {
        val original = PlaybackState(
            isPlaying = true,
            currentPosition = 50000L,
            duration = 180000L
        )

        val copied = original.copy(isPlaying = false)

        assertFalse(copied.isPlaying)
        assertEquals(50000L, copied.currentPosition)
        assertEquals(180000L, copied.duration)
    }

    // ==================== RepeatMode Tests ====================

    @Test
    fun `RepeatMode 枚举值正确`() {
        assertEquals(3, RepeatMode.entries.size)
        assertNotNull(RepeatMode.OFF)
        assertNotNull(RepeatMode.ONE)
        assertNotNull(RepeatMode.ALL)
    }

    // ==================== MoodTheme Tests ====================

    @Test
    fun `MoodTheme 枚举包含10个主题`() {
        assertEquals(10, MoodTheme.entries.size)
    }

    @Test
    fun `MoodTheme 所有主题都有名称`() {
        MoodTheme.entries.forEach { theme ->
            assertTrue(theme.displayName.isNotEmpty())
        }
    }

    @Test
    fun `MoodTheme 所有主题都有颜色值`() {
        MoodTheme.entries.forEach { theme ->
            assertNotNull(theme.primaryColor)
            assertNotNull(theme.secondaryColor)
            assertNotNull(theme.backgroundColor)
            assertNotNull(theme.surfaceColor)
            assertNotNull(theme.onPrimaryColor)
        }
    }

    @Test
    fun `MoodTheme HAPPY 使用喜庆主题`() {
        val theme = MoodTheme.HAPPY
        assertEquals("喜庆", theme.displayName)
        assertNotNull(theme.primaryColor)
    }

    @Test
    fun `MoodTheme SAD 使用哀伤主题`() {
        val theme = MoodTheme.SAD
        assertEquals("哀伤", theme.displayName)
        assertNotNull(theme.primaryColor)
    }

    @Test
    fun `MoodTheme CALM 使用平静主题`() {
        val theme = MoodTheme.CALM
        assertEquals("平静", theme.displayName)
        assertNotNull(theme.primaryColor)
    }

    @Test
    fun `MoodTheme ROMANTIC 使用浪漫主题`() {
        val theme = MoodTheme.ROMANTIC
        assertEquals("浪漫", theme.displayName)
    }

    @Test
    fun `MoodTheme NATURAL 使用自然主题`() {
        val theme = MoodTheme.NATURAL
        assertEquals("自然", theme.displayName)
    }

    @Test
    fun `getAllMoodThemes 返回所有主题`() {
        val themes = getAllMoodThemes()
        assertEquals(10, themes.size)
        assertTrue(themes.contains(MoodTheme.HAPPY))
        assertTrue(themes.contains(MoodTheme.SAD))
    }
}
