package com.sm.musicplayer.ui.screens.playlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.ui.PlayerViewModel
import com.sm.musicplayer.ui.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    isLoading: Boolean,
    currentSong: Song?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onPlayAll: () -> Unit,
    onSongClick: (Song, Int) -> Unit,
    onRemoveSong: (Song) -> Unit,
    onDeletePlaylist: () -> Unit,
    viewModel: PlayerViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "歌单") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete playlist")
                    }
                }
            )
        },
        floatingActionButton = {
            if (playlist != null && playlist.songs.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onPlayAll,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play all")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                playlist == null -> {
                    Text(
                        text = "歌单不存在",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                playlist.songs.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "歌单为空",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "从本地音乐添加歌曲到歌单",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${playlist.songs.size} 首歌曲",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(onClick = onPlayAll) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("播放全部")
                                }
                            }
                        }
                        items(
                            items = playlist.songs,
                            key = { it.id }
                        ) { song ->
                            SongListItem(
                                song = song,
                                isPlaying = currentSong?.id == song.id && isPlaying,
                                onSongClick = { onSongClick(song, playlist.songs.indexOf(song)) },
                                onFavoriteClick = { viewModel.toggleFavorite(song) },
                                onPopularClick = { viewModel.togglePopular(song) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除歌单") },
            text = { Text("确定要删除歌单 \"${playlist?.name}\" 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlaylist()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
