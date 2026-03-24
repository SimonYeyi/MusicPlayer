package com.musicplayer.presentation.ui.screens.settings

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
 * SettingsScreen UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class SettingsScreenTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysSettingsTitle() {
        composeTestRule.onNodeWithText("设置")
            .assertIsDisplayed()
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
    fun displaysMoodThemeSection() {
        composeTestRule.onNodeWithText("心情主题")
            .assertIsDisplayed()
    }

    @Test
    fun displaysPlaybackSettingsSection() {
        composeTestRule.onNodeWithText("播放设置")
            .assertIsDisplayed()
    }

    @Test
    fun displaysAboutSection() {
        composeTestRule.onNodeWithText("关于")
            .assertIsDisplayed()
    }

    @Test
    fun displaysThemeSelectorButton() {
        composeTestRule.onNodeWithText("选择心情主题")
            .assertIsDisplayed()
    }

    @Test
    fun themeSelectorButtonIsClickable() {
        composeTestRule.onNodeWithText("选择心情主题")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun displaysVersionInfo() {
        composeTestRule.onNodeWithText("版本")
            .assertIsDisplayed()
    }
}
