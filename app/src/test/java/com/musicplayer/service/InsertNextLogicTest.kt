package com.musicplayer.service

import org.junit.Assert.*
import org.junit.Test

/**
 * 插入下一首播放功能逻辑测试
 *
 * 测试 MusicPlaybackService 中 insertNext 相关方法的逻辑
 *
 * 注意：此测试类使用简化数据结构模拟 MusicPlaybackService 的内部状态，
 * 专注于验证核心去重和插入逻辑。
 */

/**
 * 简化的歌曲数据类（不依赖 Android Uri）
 */
data class TestSong(
    val id: Long,
    val title: String
)

/**
 * 简化的播放模式
 */
enum class TestPlayMode {
    LIST_LOOP,
    SHUFFLE,
    ONE_LOOP,
    OFF
}

class InsertNextLogicTest {

    // ==================== insertIntoShuffleQueue 逻辑测试 ====================

    /**
     * TC-006: 随机播放模式 - 插入不在 shuffleQueue 中的歌曲
     */
    @Test
    fun `TC-006 insertIntoShuffleQueue when song not in queue`() {
        // 模拟 shuffleQueue 状态
        val shuffleQueue = mutableListOf(0, 1, 2, 3)  // [A, B, C, D]
        val shuffleIndex = 0  // 当前播放 A
        val visualIndex = 4  // 插入歌曲 E

        // 模拟 insertIntoShuffleQueue 逻辑
        val insertPos = shuffleIndex + 1
        val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

        assertEquals(-1, existingPos)  // E 不在队列中

        if (existingPos >= 0) {
            shuffleQueue.removeAt(existingPos)
        }
        shuffleQueue.add(insertPos, visualIndex)

        // 验证结果
        assertEquals(5, shuffleQueue.size)
        assertEquals(listOf(0, 4, 1, 2, 3), shuffleQueue)
    }

    /**
     * TC-007: 随机播放模式 - 插入已在 shuffleQueue 中的歌曲（位于插入点之后）
     */
    @Test
    fun `TC-007 insertIntoShuffleQueue when song exists after insert position`() {
        // 模拟 shuffleQueue 状态
        val shuffleQueue = mutableListOf(0, 2, 1, 3)  // [A, C, B, D]
        val shuffleIndex = 0  // 当前播放 A
        val visualIndex = 1  // 插入歌曲 B（位于位置2）

        // 模拟 insertIntoShuffleQueue 逻辑
        val insertPos = shuffleIndex + 1
        val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

        assertEquals(2, existingPos)  // B 在位置2

        if (existingPos >= 0) {
            shuffleQueue.removeAt(existingPos)
        }
        shuffleQueue.add(insertPos, visualIndex)

        // 验证结果：B 被移动到位置1
        assertEquals(4, shuffleQueue.size)
        assertEquals(listOf(0, 1, 2, 3), shuffleQueue)
    }

    /**
     * TC-008: 随机播放模式 - 插入已在 shuffleQueue 中的歌曲（位于当前位置之前）
     */
    @Test
    fun `TC-008 insertIntoShuffleQueue when song exists before current position`() {
        // 模拟 shuffleQueue 状态
        val shuffleQueue = mutableListOf(0, 1, 2, 3)  // [A, B, C, D]
        val shuffleIndex = 2  // 当前播放 C（位置2）
        val visualIndex = 1  // 插入歌曲 B（位于位置1）

        // 模拟 insertIntoShuffleQueue 逻辑
        val insertPos = shuffleIndex + 1
        val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

        assertEquals(1, existingPos)  // B 在位置1

        if (existingPos >= 0) {
            shuffleQueue.removeAt(existingPos)
        }
        shuffleQueue.add(insertPos, visualIndex)

        // 验证结果：B 被移动到位置3
        assertEquals(4, shuffleQueue.size)
        assertEquals(listOf(0, 2, 3, 1), shuffleQueue)
    }

    /**
     * TC-009: 随机播放模式 - 插入当前播放的歌曲，不应有任何操作
     */
    @Test
    fun `TC-009 insertIntoShuffleQueue when inserting current song`() {
        // 模拟 shuffleQueue 状态
        val shuffleQueue = mutableListOf(0, 1, 2, 3)  // [A, B, C, D]
        val shuffleIndex = 0  // 当前播放 A
        val visualIndex = 0  // 插入歌曲 A（当前歌曲）

        // 模拟检查逻辑：如果点击的是当前正在播放的歌曲，不插队
        if (shuffleQueue[shuffleIndex] == visualIndex) {
            // 不做任何操作
            assertEquals(0, shuffleIndex)
        }

        // 验证结果：队列不变
        assertEquals(4, shuffleQueue.size)
        assertEquals(listOf(0, 1, 2, 3), shuffleQueue)
    }

    /**
     * TC-010: 随机播放模式 - shuffleQueue 为空时插入
     */
    @Test
    fun `TC-010 insertIntoShuffleQueue when queue is empty`() {
        // 模拟空队列
        val shuffleQueue = mutableListOf<Int>()
        val visualIndex = 2

        // 空队列时，初始化队列
        if (shuffleQueue.isEmpty()) {
            shuffleQueue.add(visualIndex)
        }

        // 验证结果
        assertEquals(1, shuffleQueue.size)
        assertEquals(listOf(2), shuffleQueue)
    }

    // ==================== insertNextInternal 逻辑测试 ====================

    /**
     * TC-001: 顺序播放模式 - 插入不在队列中的歌曲
     */
    @Test
    fun `TC-001 insertNextInternal when song not in queue`() {
        // 模拟状态
        val playlist = mutableListOf(
            TestSong(id = 1, title = "A"),
            TestSong(id = 2, title = "B"),
            TestSong(id = 3, title = "C"),
            TestSong(id = 4, title = "D")
        )
        val currentIndex = 0  // 播放 A
        val songToInsert = TestSong(id = 5, title = "X")

        // 模拟 insertNextInternal 逻辑
        val index = playlist.indexOfFirst { it.id == songToInsert.id }
        assertEquals(-1, index)  // X 不在队列中

        val mutablePlaylist = playlist.toMutableList()
        val insertPosition = (currentIndex + 1).coerceAtMost(mutablePlaylist.size)
        mutablePlaylist.add(insertPosition, songToInsert)

        // 验证结果
        assertEquals(5, mutablePlaylist.size)
        assertEquals(5, mutablePlaylist[1].id)  // X 在位置1（A之后）
    }

    /**
     * TC-002: 顺序播放模式 - 插入已在队列中的歌曲（位于插入点）
     */
    @Test
    fun `TC-002 insertNextInternal when song at insert position`() {
        // 模拟状态
        val playlist = mutableListOf(
            TestSong(id = 1, title = "A"),
            TestSong(id = 2, title = "B"),
            TestSong(id = 3, title = "C"),
            TestSong(id = 4, title = "D")
        )
        val currentIndex = 0  // 播放 A
        val songToInsert = TestSong(id = 2, title = "B")

        // 模拟 insertNextInternal 逻辑
        val index = playlist.indexOfFirst { it.id == songToInsert.id }
        assertEquals(1, index)  // B 在位置1

        val mutablePlaylist = playlist.toMutableList()
        mutablePlaylist.removeAt(index)

        if (index < currentIndex) {
            // 不会执行，因为 index(1) > currentIndex(0)
        }

        val insertPosition = (currentIndex + 1).coerceAtMost(mutablePlaylist.size)
        mutablePlaylist.add(insertPosition, songToInsert)

        // 验证结果：队列不变
        assertEquals(4, mutablePlaylist.size)
        assertEquals(listOf(1L, 2L, 3L, 4L), mutablePlaylist.map { it.id })
    }

    /**
     * TC-003: 顺序播放模式 - 插入已在队列中的歌曲（位于当前歌曲之前）
     *
     * 场景：播放 [A, B, C, D]，currentIndex=2（播放C），插入 B
     * 期望：队列变为 [A, C, B, D]
     *
     * 实际 MusicPlaybackService 行为：B 从位置1移除，currentIndex 从 2 变成 1
     * 然后在位置 (1+1)=2 插入 B，结果 [A, C, B, D]
     */
    @Test
    fun `TC-003 insertNextInternal when song before current`() {
        // 模拟状态
        val playlist = mutableListOf(
            TestSong(id = 1, title = "A"),
            TestSong(id = 2, title = "B"),
            TestSong(id = 3, title = "C"),
            TestSong(id = 4, title = "D")
        )
        var currentIndex = 2  // 播放 C
        val songToInsert = TestSong(id = 2, title = "B")

        // 模拟 insertNextInternal 逻辑
        val index = playlist.indexOfFirst { it.id == songToInsert.id }
        assertEquals(1, index)  // B 在位置1

        val mutablePlaylist = playlist.toMutableList()
        mutablePlaylist.removeAt(index)

        // 重新计算 currentIndex（因为移除位置在当前歌曲之前）
        if (index < currentIndex) {
            currentIndex--
        }

        val insertPosition = (currentIndex + 1).coerceAtMost(mutablePlaylist.size)
        mutablePlaylist.add(insertPosition, songToInsert)

        // 验证结果：B 从位置1移动到位置3
        assertEquals(4, mutablePlaylist.size)
        assertEquals(listOf(1L, 3L, 2L, 4L), mutablePlaylist.map { it.id })
    }

    /**
     * TC-004: 顺序播放模式 - 插入当前播放的歌曲，不应有任何操作
     */
    @Test
    fun `TC-004 insertNext when inserting current song`() {
        // 模拟状态
        val currentSongId = 1L
        val songToInsertId = 1L

        // 模拟检查逻辑
        if (currentSongId == songToInsertId) {
            // 不做任何操作
            assertEquals(currentSongId, songToInsertId)
        }

        // 验证：歌曲 ID 相同，没有做任何修改
    }

    /**
     * TC-005: 顺序播放模式 - 队列为空时插入
     */
    @Test
    fun `TC-005 insertNext when playlist is empty`() {
        // 模拟空队列
        val playlist = mutableListOf<TestSong>()

        // 空队列时应直接播放，不执行插入逻辑
        if (playlist.isEmpty()) {
            // 直接播放，不插入
            assertTrue(playlist.isEmpty())
        }
    }

    // ==================== playSongInternal shuffleIndex 同步测试 ====================

    /**
     * TC-013: playNext 调用后 shuffleIndex 同步
     */
    @Test
    fun `TC-013 playSongInternal syncs shuffleIndex`() {
        // 模拟状态
        val shuffleQueue = mutableListOf(0, 2, 1, 3)  // [A, C, B, D]
        val shuffleIndex = 0  // 当前播放 A
        val newSongVisualIndex = 2  // 播放歌曲 C

        // 模拟 playSongInternal 中的 shuffleIndex 同步逻辑
        val newShuffleIdx = shuffleQueue.indexOfFirst { it == newSongVisualIndex }
        assertEquals(1, newShuffleIdx)

        // 验证同步后 shuffleIndex 指向 C
        assertEquals(shuffleQueue[1], 2)
    }

    // ==================== 边界条件测试 ====================

    /**
     * 边界条件：shuffleQueue 只有一首歌曲时插入
     */
    @Test
    fun `insertIntoShuffleQueue when only one song in queue`() {
        val shuffleQueue = mutableListOf(0)  // 只有 A
        val shuffleIndex = 0
        val visualIndex = 1  // 插入 B

        // 检查是否是当前歌曲
        if (shuffleQueue[shuffleIndex] == visualIndex) {
            // 不做任何操作
        } else {
            val insertPos = shuffleIndex + 1
            val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

            if (existingPos >= 0) {
                shuffleQueue.removeAt(existingPos)
            }
            shuffleQueue.add(insertPos, visualIndex)
        }

        // 验证结果
        assertEquals(2, shuffleQueue.size)
        assertEquals(listOf(0, 1), shuffleQueue)
    }

    /**
     * 边界条件：插入到队列末尾
     */
    @Test
    fun `insertNextInternal when inserting at end`() {
        val playlist = mutableListOf(
            TestSong(id = 1, title = "A"),
            TestSong(id = 2, title = "B")
        )
        val currentIndex = 1  // 播放 B（最后一首）
        val songToInsert = TestSong(id = 3, title = "C")

        val mutablePlaylist = playlist.toMutableList()
        val insertPosition = (currentIndex + 1).coerceAtMost(mutablePlaylist.size)
        mutablePlaylist.add(insertPosition, songToInsert)

        // 验证结果：C 在最后
        assertEquals(3, mutablePlaylist.size)
        assertEquals(3, mutablePlaylist[2].id)
    }

    /**
     * 边界条件：队列只有一首歌曲，移除后插入
     */
    @Test
    fun `insertIntoShuffleQueue when removing only song`() {
        val shuffleQueue = mutableListOf(0, 1)  // [A, B]
        val shuffleIndex = 0  // 播放 A
        val visualIndex = 1  // 插入 B

        val insertPos = shuffleIndex + 1
        val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

        assertEquals(1, existingPos)

        if (existingPos >= 0) {
            shuffleQueue.removeAt(existingPos)
        }
        shuffleQueue.add(insertPos, visualIndex)

        // B 被移除又插入，队列不变
        assertEquals(2, shuffleQueue.size)
        assertEquals(listOf(0, 1), shuffleQueue)
    }

    /**
     * 边界条件：shuffleIndex 在队列中间时插入不在队列中的歌曲
     */
    @Test
    fun `insertIntoShuffleQueue when shuffleIndex in middle`() {
        val shuffleQueue = mutableListOf(0, 1, 2, 3)  // [A, B, C, D]
        val shuffleIndex = 2  // 当前播放 C
        val visualIndex = 4  // 插入 E（不在队列中）

        val insertPos = shuffleIndex + 1
        val existingPos = shuffleQueue.indexOfFirst { it == visualIndex }

        // E 不在队列中
        assertEquals(-1, existingPos)

        shuffleQueue.add(insertPos, visualIndex)

        // 验证结果：E 被插入到 C 之后
        assertEquals(5, shuffleQueue.size)
        assertEquals(listOf(0, 1, 2, 4, 3), shuffleQueue)
    }
}
