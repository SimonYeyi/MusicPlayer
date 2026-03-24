package com.musicplayer.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.musicplayer.presentation.ui.screens.mymusic.MyMusicScreen
import com.musicplayer.presentation.ui.screens.playdetail.PlayDetailScreen
import com.musicplayer.presentation.ui.screens.playlist.PlaylistScreen
import com.musicplayer.presentation.ui.screens.settings.SettingsScreen
import com.musicplayer.presentation.viewmodel.MusicViewModel
import kotlin.math.roundToInt

@Composable
fun MusicNavHost(
    navController: NavHostController,
    viewModel: MusicViewModel,
    showMiniPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "my_music",
        modifier = modifier
    ) {
        composable("my_music") {
            MyMusicScreen(
                viewModel = viewModel,
                onNavigateToPlayDetail = {
                    navController.navigate("play_detail")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToPlaylist = { playlistId, playlistName ->
                    navController.navigate("playlist/$playlistId/$playlistName")
                }
            )
        }
        composable(
            route = "play_detail",
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            PlayDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("playlist/{playlistId}/{playlistName}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
            val playlistName = backStackEntry.arguments?.getString("playlistName") ?: "歌单"
            PlaylistScreen(
                playlistId = playlistId,
                playlistName = playlistName,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToPlaylist = { targetId, targetName ->
                    navController.navigate("playlist/$targetId/$targetName")
                },
                onNavigateToPlaylistAndPop = { targetId, targetName ->
                    navController.popBackStack()
                    navController.navigate("playlist/$targetId/$targetName")
                },
                showMiniPlayer = showMiniPlayer
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToTab = { tab ->
                    viewModel.setSelectedMyMusicTab(tab)
                    navController.popBackStack()
                }
            )
        }
    }
}
