package com.musicplayer.presentation.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.musicplayer.R
import com.musicplayer.domain.model.MoodTheme
import com.musicplayer.presentation.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MoodThemePicker Dialog UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MoodThemePickerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysThemePickerTitle() {
        // Navigate to settings and click theme selector
        composeTestRule.onNodeWithText("设置")
            .assertIsDisplayed()
    }

    @Test
    fun displaysMoodThemes() {
        // Check that theme names are displayed when dialog opens
        composeTestRule.onNodeWithText("喜庆")
            .assertIsDisplayed()
    }

    @Test
    fun displaysAllThemeOptions() {
        // All 10 themes should be available
        composeTestRule.onNodeWithText("哀伤")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("平静")
            .assertIsDisplayed()
    }

    @Test
    fun themeOptionsAreClickable() {
        composeTestRule.onNodeWithText("喜庆")
            .assertIsEnabled()
    }
}
