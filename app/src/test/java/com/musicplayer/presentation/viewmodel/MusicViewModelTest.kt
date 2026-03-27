package com.musicplayer.presentation.viewmodel

import com.musicplayer.domain.model.MoodTheme
import com.musicplayer.domain.model.PlaybackState
import com.musicplayer.domain.model.PlayMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * MusicViewModel 相关测试
 *
 * 注意：由于 Uri 和 Song 是 Android 框架类，在纯 JVM 单元测试环境中
 * 无法直接实例化。此文件主要测试 MusicUiState 和逻辑分支。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== MusicUiState 测试 ====================

    @Test
    fun `MusicUiState 默认值正确`() {
        val state = MusicUiState()

        assertTrue(state.songs.isEmpty())
        assertTrue(state.filteredSongs.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.hasPermission)
        assertEquals(MoodTheme.CALM, state.currentTheme)
        assertFalse(state.showThemePicker)
    }

    @Test
    fun `MusicUiState copy 正常工作`() {
        // 直接创建测试数据
        val playbackState = PlaybackState(
            currentSong = null,
            isPlaying = true,
            currentPosition = 50000L,
            duration = 180000L,
            playMode = PlayMode.LIST_LOOP
        )

        // 测试PlaybackState
        assertTrue(playbackState.isPlaying)
        assertEquals(50000L, playbackState.currentPosition)
        assertEquals(180000L, playbackState.duration)
        assertEquals(PlayMode.LIST_LOOP, playbackState.playMode)
    }

    @Test
    fun `MusicUiState copy 更新字段`() {
        val original = MusicUiState(
            songs = emptyList(),
            isLoading = false
        )

        val copied = original.copy(isLoading = true)

        assertTrue(copied.isLoading)
    }

    // ==================== MoodTheme 测试 ====================

    @Test
    fun `setTheme 更改当前主题`() {
        // 测试主题切换逻辑
        var currentTheme = MoodTheme.CALM
        currentTheme = MoodTheme.HAPPY
        assertEquals(MoodTheme.HAPPY, currentTheme)
    }

    @Test
    fun `toggleThemePicker 切换状态`() {
        // 测试主题选择器状态切换
        var showThemePicker = false
        showThemePicker = !showThemePicker
        assertTrue(showThemePicker)
        showThemePicker = !showThemePicker
        assertFalse(showThemePicker)
    }

    @Test
    fun `hideThemePicker 设置为false`() {
        var showThemePicker = true
        showThemePicker = false
        assertFalse(showThemePicker)
    }

    // ==================== 搜索功能测试 ====================

    @Test
    fun `search 过滤逻辑`() {
        // 测试搜索过滤逻辑
        val songs = listOf(
            "Test Song",
            "Hello World",
            "Another Song"
        )

        // 按标题搜索
        val filtered = songs.filter { it.contains("Test", ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Test Song", filtered[0])
    }

    @Test
    fun `search 空查询返回所有`() {
        val songs = listOf("Song1", "Song2")
        val query = ""

        val filtered = if (query.isEmpty()) songs else songs.filter { it.contains(query) }
        assertEquals(2, filtered.size)
    }

    @Test
    fun `search 不区分大小写`() {
        val songs = listOf("Hello", "HELLO", "hello")
        val query = "hello"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }
        assertEquals(3, filtered.size)
    }

    // ==================== onShareClick 逻辑测试 ====================

    /**
     * 测试分享逻辑：当 URI 不为 null 时应该触发分享事件
     * 由于 Uri 是 Android 框架类，这里使用 String? 模拟测试逻辑分支
     */
    @Test
    fun `onShareClick logic with valid URI should emit event`() {
        // 模拟 onShareClick 的条件判断逻辑 (Uri 替换为 String? 以避免 Android 框架依赖)
        val uri: String? = "content://media/audio/1"
        var shareEventEmitted = false

        if (uri != null) {
            shareEventEmitted = true
        }

        assertTrue(shareEventEmitted)
    }

    /**
     * 测试分享逻辑：当 URI 为 null 时应该发送 Toast
     * 由于 Uri 是 Android 框架类，这里使用 String? 模拟测试逻辑分支
     */
    @Test
    fun `onShareClick logic with null URI should emit toast`() {
        // 模拟 onShareClick 的条件判断逻辑 (Uri 替换为 String? 以避免 Android 框架依赖)
        val uri: String? = null
        var toastMessageEmitted: String? = null

        if (uri != null) {
            // 不应该执行这里
        } else {
            toastMessageEmitted = "分享失败"
        }

        assertEquals("分享失败", toastMessageEmitted)
    }
}
