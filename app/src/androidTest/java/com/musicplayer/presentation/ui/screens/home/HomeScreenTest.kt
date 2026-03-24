package com.musicplayer.presentation.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
 * HomeScreen UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class HomeScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysAppName() {
        composeTestRule.onNodeWithText("音乐播放器")
            .assertIsDisplayed()
    }

    @Test
    fun displaysSearchButton() {
        composeTestRule.onNodeWithContentDescription("搜索")
            .assertIsDisplayed()
    }

    @Test
    fun displaysSettingsButton() {
        composeTestRule.onNodeWithContentDescription("设置")
            .assertIsDisplayed()
    }

    @Test
    fun settingsButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("设置")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun searchButtonIsClickable() {
        composeTestRule.onNodeWithContentDescription("搜索")
            .assertIsEnabled()
            .performClick()
    }
}
