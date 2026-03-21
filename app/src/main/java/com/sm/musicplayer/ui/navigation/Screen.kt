package com.sm.musicplayer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Library : Screen(
        route = "library",
        title = "本地音乐",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    )

    data object Popular : Screen(
        route = "popular",
        title = "热门歌曲",
        selectedIcon = Icons.Filled.Whatshot,
        unselectedIcon = Icons.Outlined.Whatshot
    )

    data object Playlist : Screen(
        route = "playlist",
        title = "歌单",
        selectedIcon = Icons.Filled.QueueMusic,
        unselectedIcon = Icons.Outlined.QueueMusic
    )

    data object Favorites : Screen(
        route = "favorites",
        title = "我的收藏",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    data object Player : Screen(
        route = "player",
        title = "正在播放",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic
    )
}

val bottomNavItems = listOf(
    Screen.Library,
    Screen.Popular,
    Screen.Playlist,
    Screen.Favorites
)
