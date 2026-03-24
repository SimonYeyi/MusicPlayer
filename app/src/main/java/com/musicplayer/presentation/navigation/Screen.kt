package com.musicplayer.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MusicLibrary : Screen("music_library")
    object MyMusic : Screen("my_music")
    object Playlist : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object PlayDetail : Screen("play_detail")
}
