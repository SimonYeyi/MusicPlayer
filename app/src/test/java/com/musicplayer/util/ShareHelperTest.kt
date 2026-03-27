package com.musicplayer.util

import org.junit.Assert.*
import org.junit.Test

/**
 * ShareHelper 单元测试
 *
 * 注意：ShareHelper.shareSong 依赖 Android 框架类 (Context, Intent, Uri 等)，
 * 在纯 JVM 单元测试环境中无法完整测试。此文件验证 ShareHelper 类的基本结构。
 *
 * 完整的 Android 框架测试需要在 instrumented tests 或使用 Robolectric 的环境中进行。
 */
class ShareHelperTest {

    /**
     * 验证 ShareHelper 类存在
     */
    @Test
    fun `ShareHelper class exists`() {
        // Verify the ShareHelper class exists and can be referenced
        assertNotNull(ShareHelper::class)
    }

    /**
     * 验证 ShareHelper.shareSong 方法存在
     */
    @Test
    fun `ShareHelper shareSong method exists`() {
        // 获取 ShareHelper.shareSong 方法引用，验证方法存在
        val method = ShareHelper::class.java.getMethod("shareSong", android.content.Context::class.java, com.musicplayer.domain.model.Song::class.java)
        assertNotNull(method)
    }

    /**
     * 验证 ShareHelper.shareSong 方法返回 Boolean 类型
     */
    @Test
    fun `ShareHelper shareSong returns Boolean`() {
        val method = ShareHelper::class.java.getMethod("shareSong", android.content.Context::class.java, com.musicplayer.domain.model.Song::class.java)
        assertEquals(Boolean::class.java, method.returnType)
    }
}
