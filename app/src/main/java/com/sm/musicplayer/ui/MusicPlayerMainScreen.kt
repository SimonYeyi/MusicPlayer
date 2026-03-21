package com.sm.musicplayer.ui

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.ui.components.MiniPlayer
import com.sm.musicplayer.ui.navigation.Screen
import com.sm.musicplayer.ui.navigation.bottomNavItems
import com.sm.musicplayer.ui.screens.favorites.FavoritesScreen
import com.sm.musicplayer.ui.screens.library.AddToPlaylistDialog
import com.sm.musicplayer.ui.screens.library.LibraryScreen
import com.sm.musicplayer.ui.screens.player.FullPlayerScreen
import com.sm.musicplayer.ui.screens.playlist.PlaylistDetailScreen
import com.sm.musicplayer.ui.screens.playlist.PlaylistScreen
import com.sm.musicplayer.ui.screens.popular.PopularScreen
import com.sm.musicplayer.ui.screens.search.SearchScreen
import com.sm.musicplayer.ui.screens.settings.SettingsScreen
import com.sm.musicplayer.ui.theme.MusicPlayerTheme
import kotlinx.coroutines.flow.collectLatest

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
    val currentMoodTheme by viewModel.currentMoodTheme.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var selectedSongForPlaylist by remember { mutableStateOf<Song?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showPlaylistDetail by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.uiState.collectLatest { state ->
            showSettings = state.showSettings
        }
    }

    MusicPlayerTheme(
        moodTheme = currentMoodTheme,
        isDarkMode = isDarkMode
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    if (!showSettings && !showSearch && !showPlaylistDetail) {
                        TopAppBar(
                            title = {
                                Text(
                                    when (uiState.currentTab) {
                                        0 -> "本地音乐"
                                        1 -> "热门歌曲"
                                        2 -> "歌单"
                                        3 -> "我的收藏"
                                        else -> "音乐"
                                    }
                                )
                            },
                            actions = {
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(Icons.Filled.Search, contentDescription = "搜索")
                                }
                                IconButton(onClick = { showSettings = true }) {
                                    Icon(Icons.Filled.Settings, contentDescription = "设置")
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (!showSettings && !showSearch && !showPlaylistDetail) {
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
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when {
                        showSettings -> {
                            SettingsScreen(
                                currentMoodTheme = currentMoodTheme,
                                isDarkMode = isDarkMode,
                                onMoodThemeChange = { viewModel.setMoodTheme(it) },
                                onDarkModeChange = { viewModel.setDarkMode(it) },
                                onBackClick = { showSettings = false },
                                onClearCache = { },
                                onAboutClick = { }
                            )
                        }
                        showSearch -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                searchResults = searchResults,
                                isSearching = isSearching,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onSearchQueryChange = { viewModel.search(it) },
                                onClearSearch = { viewModel.clearSearch() },
                                onSongClick = { song, index ->
                                    viewModel.playSongs(searchResults, index)
                                    viewModel.showMiniPlayer()
                                },
                                onBackClick = { showSearch = false },
                                viewModel = viewModel
                            )
                        }
                        showPlaylistDetail && selectedPlaylist != null -> {
                            val playlistFlow = remember(selectedPlaylist?.id) {
                                selectedPlaylist?.id?.let { viewModel.getPlaylistWithSongs(it) }
                            }
                            val playlistWithSongs by playlistFlow?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
                            
                            PlaylistDetailScreen(
                                playlist = playlistWithSongs,
                                isLoading = playlistWithSongs == null,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onBackClick = { 
                                    showPlaylistDetail = false
                                    selectedPlaylist = null
                                },
                                onPlayAll = {
                                    playlistWithSongs?.songs?.let { songs ->
                                        if (songs.isNotEmpty()) {
                                            viewModel.playSongs(songs, 0)
                                            viewModel.showMiniPlayer()
                                        }
                                    }
                                },
                                onSongClick = { song, index ->
                                    playlistWithSongs?.songs?.let { songs ->
                                        viewModel.playSongs(songs, index)
                                        viewModel.showMiniPlayer()
                                    }
                                },
                                onRemoveSong = { song ->
                                    selectedPlaylist?.id?.let { playlistId ->
                                        viewModel.removeSongFromPlaylist(playlistId, song.id)
                                    }
                                },
                                onDeletePlaylist = {
                                    selectedPlaylist?.let { viewModel.deletePlaylist(it) }
                                    showPlaylistDetail = false
                                    selectedPlaylist = null
                                },
                                viewModel = viewModel
                            )
                        }
                        else -> {
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
                                    onAddToPlaylistClick = { song ->
                                        selectedSongForPlaylist = song
                                        showAddToPlaylistDialog = true
                                    },
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
                                        selectedPlaylist = playlist
                                        showPlaylistDetail = true
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

        if (showAddToPlaylistDialog && selectedSongForPlaylist != null) {
            AddToPlaylistDialog(
                playlists = playlists,
                onPlaylistSelected = { playlist ->
                    selectedSongForPlaylist?.let { song ->
                        viewModel.addSongToPlaylist(playlist.id, song.id)
                    }
                },
                onCreatePlaylist = {
                    viewModel.createPlaylist("新歌单")
                },
                onDismiss = {
                    showAddToPlaylistDialog = false
                    selectedSongForPlaylist = null
                }
            )
        }
    }
}
