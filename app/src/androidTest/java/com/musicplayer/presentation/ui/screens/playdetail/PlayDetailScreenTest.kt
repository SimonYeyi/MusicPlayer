package com.musicplayer.presentation.ui.screens.playdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.musicplayer.R
import com.musicplayer.presentation.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * PlayDetailScreen UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PlayDetailScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysBackButton() {
        composeTestRule.onNodeWithContentDescription("返回")
            .assertIsDisplayed()
    }

    @Test
    fun backButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("返回")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun displaysPlayControlButton() {
        composeTestRule.onNodeWithContentDescription("播放")
            .assertIsDisplayed()
    }

    @Test
    fun displaysPreviousButton() {
        composeTestRule.onNodeWithContentDescription("上一首")
            .assertIsDisplayed()
    }

    @Test
    fun displaysNextButton() {
        composeTestRule.onNodeWithContentDescription("下一首")
            .assertIsDisplayed()
    }

    @Test
    fun previousButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("上一首")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun nextButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("下一首")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun displaysShuffleButton() {
        composeTestRule.onNodeWithContentDescription("随机播放")
            .assertIsDisplayed()
    }

    @Test
    fun displaysRepeatButton() {
        composeTestRule.onNodeWithContentDescription("循环模式")
            .assertIsDisplayed()
    }
}
