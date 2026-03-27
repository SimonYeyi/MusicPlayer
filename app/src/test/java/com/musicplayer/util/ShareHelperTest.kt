package com.musicplayer.util

import android.content.Context
import com.musicplayer.domain.model.Song
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * ShareHelper 单元测试
 *
 * 注意：ShareHelper.shareSong 依赖 Android 框架类 (Context, Intent, Uri 等)，
 * 在纯 JVM 单元测试环境中无法完整测试。这些测试验证 ShareHelper 的方法签名
 * 和基本结构完整性。
 *
 * 完整的 Android 框架测试需要在 instrumented tests 或使用 Robolectric 的环境中进行。
 */
class ShareHelperTest {

    @Test
    fun `ShareHelper class exists`() {
        assertNotNull(ShareHelper::class)
    }

    @Test
    fun `shareSong method exists and returns Boolean`() {
        val method = ShareHelper::class.java.getMethod("shareSong", Context::class.java, Song::class.java)
        assertNotNull(method)
        assertEquals(Boolean::class.java, method.returnType)
    }

    @Test
    fun `shareSong method accepts Context and Song parameters`() {
        val method = ShareHelper::class.java.getMethod("shareSong", Context::class.java, Song::class.java)
        val parameterTypes = method.parameterTypes
        assertEquals(2, parameterTypes.size)
        assertEquals(Context::class.java, parameterTypes[0])
        assertEquals(Song::class.java, parameterTypes[1])
    }
}
