package com.musicplayer.presentation.ui.screens.mymusic

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.musicplayer.presentation.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MyMusicScreen UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MyMusicScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysMyMusicTitle() {
        composeTestRule.onNodeWithText("我的")
            .assertIsDisplayed()
    }

    @Test
    fun displaysLocalMusicTab() {
        composeTestRule.onNodeWithText("本地音乐")
            .assertIsDisplayed()
    }
}
