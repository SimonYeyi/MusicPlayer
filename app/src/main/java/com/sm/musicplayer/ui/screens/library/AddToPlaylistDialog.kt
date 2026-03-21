package com.sm.musicplayer.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加到歌单") },
        text = {
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("创建新歌单") },
                        leadingContent = {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            onCreatePlaylist()
                            onDismiss()
                        }
                    )
                    HorizontalDivider()
                }
                items(playlists) { playlist ->
                    ListItem(
                        headlineContent = { Text(playlist.name) },
                        supportingContent = { Text("${playlist.songCount} 首歌曲") },
                        leadingContent = {
                            Icon(Icons.Filled.MusicNote, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            onPlaylistSelected(playlist)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
