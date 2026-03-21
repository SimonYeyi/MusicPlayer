package com.sm.musicplayer.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val moodThemeKey = stringPreferencesKey("mood_theme")
    private val isDarkModeKey = stringPreferencesKey("is_dark_mode")

    val currentMoodTheme: Flow<MoodTheme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[moodThemeKey] ?: MoodTheme.PASSION_RED.name
        try {
            MoodTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            MoodTheme.PASSION_RED
        }
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isDarkModeKey]?.toBoolean() ?: true
    }

    suspend fun setMoodTheme(theme: MoodTheme) {
        context.dataStore.edit { preferences ->
            preferences[moodThemeKey] = theme.name
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDarkModeKey] = isDark.toString()
        }
    }
}
