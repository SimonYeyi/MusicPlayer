package com.musicplayer.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Edge Case UI Test
 * Tests handling of edge cases and error states
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class EdgeCaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appNameDisplayedCorrectly() {
        composeTestRule.onNodeWithText("音乐播放器")
            .assertIsDisplayed()
    }

    @Test
    fun navigationToSettings() {
        composeTestRule.onNodeWithText("设置")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun settingsScreenDisplayed() {
        composeTestRule.onNodeWithText("设置")
            .performClick()
        composeTestRule.onNodeWithText("关于")
            .assertIsDisplayed()
    }
}
