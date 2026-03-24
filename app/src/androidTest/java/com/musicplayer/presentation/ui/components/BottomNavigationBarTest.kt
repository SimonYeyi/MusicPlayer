package com.musicplayer.presentation.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * BottomNavigationBar Component UI Test
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BottomNavigationBarTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun displaysHomeTab() {
        composeTestRule.onNodeWithText("发现")
            .assertIsDisplayed()
    }

    @Test
    fun displaysMusicLibraryTab() {
        composeTestRule.onNodeWithText("音乐馆")
            .assertIsDisplayed()
    }

    @Test
    fun displaysMyMusicTab() {
        composeTestRule.onNodeWithText("我的")
            .assertIsDisplayed()
    }

    @Test
    fun homeTabIsClickable() {
        composeTestRule.onNodeWithText("发现")
            .assertIsEnabled()
    }

    @Test
    fun musicLibraryTabIsClickable() {
        composeTestRule.onNodeWithText("音乐馆")
            .assertIsEnabled()
    }

    @Test
    fun myMusicTabIsClickable() {
        composeTestRule.onNodeWithText("我的")
            .assertIsEnabled()
    }
}
