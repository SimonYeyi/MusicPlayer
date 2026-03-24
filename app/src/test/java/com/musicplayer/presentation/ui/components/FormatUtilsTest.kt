package com.musicplayer.presentation.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * FormatUtils 工具类单元测试
 * 注意: formatDuration 目前只支持分:秒格式，超过60分钟会显示为分钟数
 */
class FormatUtilsTest {

    @Test
    fun `formatDuration 正常时长格式化`() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("0:30", formatDuration(30000L))
        assertEquals("1:00", formatDuration(60000L))
        assertEquals("1:30", formatDuration(90000L))
        assertEquals("3:45", formatDuration(225000L))
    }

    @Test
    fun `formatDuration 超过一小时格式化`() {
        // 当前实现只支持分:秒，超过60分钟显示分钟数
        assertEquals("10:00", formatDuration(600000L))
        assertEquals("59:59", formatDuration(3599000L))
        // 超过60分钟会显示为60:00, 90:00等
        assertEquals("60:00", formatDuration(3600000L))
        assertEquals("90:00", formatDuration(5400000L))
    }

    @Test
    fun `formatDuration 边界值测试`() {
        // 毫秒为0
        assertEquals("0:00", formatDuration(0L))

        // 刚好一分钟
        assertEquals("1:00", formatDuration(60 * 1000L))

        // 刚好一小时
        assertEquals("60:00", formatDuration(60 * 60 * 1000L))

        // 刚好59分59秒
        assertEquals("59:59", formatDuration((59 * 60 + 59) * 1000L))

        // 超过一小时
        assertEquals("60:01", formatDuration((60 * 60 + 1) * 1000L))
    }

    @Test
    fun `formatDuration 秒数补零`() {
        assertEquals("0:01", formatDuration(1000L))
        assertEquals("0:09", formatDuration(9000L))
        assertEquals("0:10", formatDuration(10000L))
        assertEquals("1:01", formatDuration(61000L))
        assertEquals("1:09", formatDuration(69000L))
    }
}
