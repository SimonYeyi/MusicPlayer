package com.sm.musicplayer.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sm.musicplayer.ui.components.MiniPlayer
import com.sm.musicplayer.ui.navigation.Screen
import com.sm.musicplayer.ui.navigation.bottomNavItems
import com.sm.musicplayer.ui.screens.favorites.FavoritesScreen
import com.sm.musicplayer.ui.screens.library.LibraryScreen
import com.sm.musicplayer.ui.screens.player.FullPlayerScreen
import com.sm.musicplayer.ui.screens.playlist.PlaylistScreen
import com.sm.musicplayer.ui.screens.popular.PopularScreen

@Composable
fun MusicPlayerMainScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val popularSongs by viewModel.popularSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.scanMusic(context.contentResolver)
        }
    }

    LaunchedEffect(Unit) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(permission)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    AnimatedVisibility(
                        visible = uiState.showMiniPlayer && !uiState.showFullPlayer,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        MiniPlayer(
                            song = currentSong,
                            isPlaying = isPlaying,
                            onPlayPauseClick = { viewModel.playPause() },
                            onNextClick = { viewModel.skipToNext() },
                            onClick = { viewModel.showFullPlayer() }
                        )
                    }

                    NavigationBar {
                        bottomNavItems.forEachIndexed { index, screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.currentTab == index) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = uiState.currentTab == index,
                                onClick = {
                                    viewModel.uiState.value = viewModel.uiState.value.copy(currentTab = index)
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (uiState.currentTab) {
                    0 -> LibraryScreen(
                        songs = songs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, index ->
                            viewModel.playSongs(songs, index)
                            viewModel.showMiniPlayer()
                        },
                        onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                        onPopularClick = { song -> viewModel.togglePopular(song) },
                        onScanClick = { viewModel.scanMusic(context.contentResolver) },
                        isScanning = uiState.isScanning,
                        viewModel = viewModel
                    )
                    1 -> PopularScreen(
                        songs = popularSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, index ->
                            viewModel.playSongs(popularSongs, index)
                            viewModel.showMiniPlayer()
                        },
                        onPopularClick = { song -> viewModel.togglePopular(song) },
                        viewModel = viewModel
                    )
                    2 -> PlaylistScreen(
                        playlists = playlists,
                        onPlaylistClick = { playlist ->
                            if (playlist.songs.isNotEmpty()) {
                                viewModel.playSongs(playlist.songs, 0)
                                viewModel.showMiniPlayer()
                            }
                        },
                        onCreatePlaylist = { },
                        viewModel = viewModel
                    )
                    3 -> FavoritesScreen(
                        songs = favoriteSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onSongClick = { song, index ->
                            viewModel.playSongs(favoriteSongs, index)
                            viewModel.showMiniPlayer()
                        },
                        onFavoriteClick = { song -> viewModel.toggleFavorite(song) },
                        onPopularClick = { song -> viewModel.togglePopular(song) },
                        viewModel = viewModel
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            FullPlayerScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                onBackClick = { viewModel.hideFullPlayer() },
                onPlayPauseClick = { viewModel.playPause() },
                onPreviousClick = { viewModel.skipToPrevious() },
                onNextClick = { viewModel.skipToNext() },
                onSeek = { viewModel.seekToPercent(it) },
                onFavoriteClick = { currentSong?.let { song -> viewModel.toggleFavorite(song) } },
                viewModel = viewModel
            )
        }
    }
}
