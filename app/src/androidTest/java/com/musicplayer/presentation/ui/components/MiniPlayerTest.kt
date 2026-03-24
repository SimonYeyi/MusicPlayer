package com.musicplayer.presentation.ui.components

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
 * MiniPlayer Component UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MiniPlayerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private var playPauseClicked = false
    private var nextClicked = false

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysPlayButton() {
        composeTestRule.onNodeWithContentDescription("播放")
            .assertIsDisplayed()
    }

    @Test
    fun displaysPauseButtonWhenPlaying() {
        composeTestRule.onNodeWithContentDescription("暂停")
            .assertIsDisplayed()
    }

    @Test
    fun displaysNextButton() {
        composeTestRule.onNodeWithContentDescription("下一首")
            .assertIsDisplayed()
    }

    @Test
    fun playButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("播放")
            .assertIsEnabled()
    }

    @Test
    fun pauseButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("暂停")
            .assertIsEnabled()
    }

    @Test
    fun nextButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("下一首")
            .assertIsEnabled()
    }
}
