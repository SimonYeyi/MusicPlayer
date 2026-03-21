package com.sm.musicplayer.ui.screens.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.ui.PlayerViewModel
import com.sm.musicplayer.ui.components.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song, Int) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onPopularClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onScanClick: () -> Unit,
    isScanning: Boolean,
    viewModel: PlayerViewModel
) {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanClick()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "本地音乐 (${songs.size})",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    permissionLauncher.launch(permission)
                },
                enabled = !isScanning
            ) {
                if (isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Scan music"
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "暂无音乐",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右上角按钮扫描本地音乐",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(
                    items = songs,
                    key = { it.id }
                ) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = currentSong?.id == song.id && isPlaying,
                        onSongClick = { onSongClick(song, songs.indexOf(song)) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        onPopularClick = { onPopularClick(song) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
