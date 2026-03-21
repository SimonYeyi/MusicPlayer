package com.sm.musicplayer.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.ui.PlayerViewModel
import com.sm.musicplayer.ui.components.SongListItem

@Composable
fun FavoritesScreen(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song, Int) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onPopularClick: (Song) -> Unit,
    viewModel: PlayerViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "我的收藏 (${songs.size})",
                style = MaterialTheme.typography.titleLarge
            )
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
                        text = "暂无收藏",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击歌曲旁边的爱心图标添加收藏",
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
