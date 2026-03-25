package com.musicplayer.presentation.ui.screens.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.launch
import com.musicplayer.R
import com.musicplayer.domain.model.Song
import com.musicplayer.presentation.ui.components.AlbumArtImage
import com.musicplayer.presentation.ui.components.formatDuration
import com.musicplayer.presentation.ui.screens.mymusic.CreatePlaylistDialog
import com.musicplayer.presentation.ui.screens.mymusic.SelectPlaylistDialog
import com.musicplayer.presentation.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: Long,
    playlistName: String,
    viewModel: MusicViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToPlaylist: (Long, String) -> Unit,
    onNavigateToPlaylistAndPop: (Long, String) -> Unit,
    showMiniPlayer: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var playlistSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var songToRemove by remember { mutableStateOf<Long?>(null) }

    // 编辑模式状态
    var isEditMode by remember { mutableStateOf(false) }
    var selectedSongIds by remember { mutableStateOf(setOf<Long>()) }

    // 弹框状态
    var showMoveDialog by remember { mutableStateOf(false) }
    var showRemoveConfirm by remember { mutableStateOf(false) }
    var showDeletePlaylistConfirm by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var lastMovedToPlaylist by remember { mutableStateOf<Pair<Long, String>?>(null) }

    LaunchedEffect(playlistId) {
        playlistSongs = viewModel.getPlaylistSongs(playlistId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditMode) {
                        Text("已选择 ${selectedSongIds.size} 首")
                    } else {
                        Text(
                            text = playlistName,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditMode) {
                            isEditMode = false
                            selectedSongIds = emptySet()
                            lastMovedToPlaylist = null
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            if (isEditMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (isEditMode) "取消" else stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (!isEditMode && playlistSongs.isNotEmpty()) {
                        TextButton(onClick = {
                            isEditMode = true
                        }) {
                            Text("编辑")
                        }
                    } else if (isEditMode) {
                        TextButton(onClick = {
                            if (selectedSongIds.size == playlistSongs.size) {
                                selectedSongIds = emptySet()
                            } else {
                                selectedSongIds = playlistSongs.map { it.id }.toSet()
                            }
                        }) {
                            Text(
                                if (selectedSongIds.size == playlistSongs.size) "全不选" else "全选"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isEditMode && selectedSongIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!showMiniPlayer) Modifier.navigationBarsPadding() else Modifier)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { showMoveDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("移动到其他歌单", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { showRemoveConfirm = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("从歌单中移除", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (playlistSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.MusicOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "歌单暂无歌曲",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = if (isEditMode && selectedSongIds.isNotEmpty()) 80.dp else 100.dp)
            ) {
                itemsIndexed(
                    items = playlistSongs,
                    key = { _, song -> song.id }
                ) { _, song ->
                    PlaylistSongItem(
                        song = song,
                        isFavorite = uiState.favoriteSongIds.contains(song.id),
                        isEditMode = isEditMode,
                        isSelected = selectedSongIds.contains(song.id),
                        onSelectToggle = {
                            selectedSongIds = if (selectedSongIds.contains(song.id)) {
                                selectedSongIds - song.id
                            } else {
                                selectedSongIds + song.id
                            }
                        },
                        onClick = {
                            viewModel.playSongs(playlistSongs, song, playlistId = playlistId.toString())
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                        onRemoveClick = { songToRemove = song.id },
                        onMoveClick = {
                            selectedSongIds = setOf(song.id)
                            isEditMode = true
                            showMoveDialog = true
                        },
                        isCurrentSong = uiState.playbackState.currentSong?.id == song.id,
                        isCurrentlyPlaying = uiState.playbackState.isPlaying && uiState.playbackState.currentSong?.id == song.id
                    )
                }
            }
        }
    }

    // 单个移除确认对话框
    if (songToRemove != null) {
        AlertDialog(
            onDismissRequest = { songToRemove = null },
            title = { Text("移除歌曲") },
            text = { Text("确定要从歌单中移除这首歌曲吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        songToRemove?.let { songId ->
                            viewModel.removeFromPlaylist(playlistId, songId)
                            playlistSongs = playlistSongs.filter { it.id != songId }
                        }
                        songToRemove = null
                        if (playlistSongs.isEmpty()) {
                            showDeletePlaylistConfirm = true
                        }
                    }
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToRemove = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 批量移除确认对话框
    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("移除歌曲") },
            text = { Text("确定要从歌单中移除选中的 ${selectedSongIds.size} 首歌曲吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val songIdsToRemove = selectedSongIds.toList()
                        songIdsToRemove.forEach { songId ->
                            viewModel.removeFromPlaylist(playlistId, songId)
                        }
                        playlistSongs = playlistSongs.filter { it.id !in songIdsToRemove }
                        isEditMode = false
                        selectedSongIds = emptySet()
                        showRemoveConfirm = false
                        if (playlistSongs.isEmpty()) {
                            showDeletePlaylistConfirm = true
                        }
                    }
                ) {
                    Text("移除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 删除空歌单确认对话框
    if (showDeletePlaylistConfirm) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistConfirm = false },
            title = { Text("删除歌单") },
            text = { Text("歌单已为空，确定要删除歌单\"$playlistName\"吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlaylist(playlistId)
                        showDeletePlaylistConfirm = false
                        onBackClick()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeletePlaylistConfirm = false
                        if (lastMovedToPlaylist != null) {
                            val (id, name) = lastMovedToPlaylist!!
                            lastMovedToPlaylist = null
                            onNavigateToPlaylistAndPop(id, name)
                        }
                    }
                ) {
                    Text(if (lastMovedToPlaylist != null) "去到目标歌单" else "留在歌单")
                }
            }
        )
    }

    // 选择目标歌单对话框
    if (showMoveDialog) {
        val otherPlaylists = uiState.playlists.filter { it.id != playlistId }
        SelectPlaylistDialog(
            playlists = otherPlaylists,
            title = "移动到",
            onDismiss = { showMoveDialog = false },
            onPlaylistSelected = { targetPlaylistId ->
                val targetPlaylist = otherPlaylists.find { it.id == targetPlaylistId }
                val songIdsToMove = selectedSongIds.toList()
                viewModel.moveSongsToPlaylist(playlistId, targetPlaylistId, songIdsToMove)
                playlistSongs = playlistSongs.filter { it.id !in songIdsToMove }
                isEditMode = false
                selectedSongIds = emptySet()
                showMoveDialog = false
                if (playlistSongs.isEmpty()) {
                    lastMovedToPlaylist = Pair(targetPlaylistId, targetPlaylist?.name ?: "")
                    showDeletePlaylistConfirm = true
                }
            },
            onCreateClick = { showCreatePlaylistDialog = true }
        )
    }

    // 创建歌单对话框
    if (showCreatePlaylistDialog) {
        val lifecycleOwner = LocalLifecycleOwner.current
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                lifecycleOwner.lifecycleScope.launch {
                    val newPlaylistId = viewModel.createPlaylist(name)
                    showCreatePlaylistDialog = false
                    // 创建完成后视为选中该歌单，完成歌曲迁移
                    val songIdsToMove = selectedSongIds.toList()
                    viewModel.moveSongsToPlaylist(playlistId, newPlaylistId, songIdsToMove)
                    playlistSongs = playlistSongs.filter { it.id !in songIdsToMove }
                    isEditMode = false
                    selectedSongIds = emptySet()
                    showMoveDialog = false
                    if (playlistSongs.isEmpty()) {
                        lastMovedToPlaylist = Pair(newPlaylistId, name)
                        showDeletePlaylistConfirm = true
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistSongItem(
    song: Song,
    isFavorite: Boolean,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: (() -> Unit)? = null,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onMoveClick: (() -> Unit)? = null,
    isCurrentSong: Boolean = false,
    isCurrentlyPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffsetX by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .onSizeChanged { size -> itemHeight = size.height.toFloat() }
                .pointerInput(isCurrentSong, isCurrentlyPlaying, isEditMode) {
                    detectTapGestures(
                        onTap = {
                            if (isEditMode) {
                                onSelectToggle?.invoke()
                            } else {
                                onClick()
                            }
                        },
                        onLongPress = { offset ->
                            if (!isEditMode) {
                                menuOffsetX = offset.x
                                showMenu = true
                            }
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle?.invoke() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            AlbumArtImage(
                albumArtUri = song.albumArtUri,
                contentDescription = null,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${formatDuration(song.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showMenu) {
            Popup(
                offset = IntOffset(menuOffsetX.toInt(), (itemHeight / 2).toInt()),
                onDismissRequest = { showMenu = false }
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = 200.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (onMoveClick != null) {
                            DropdownMenuItem(
                                text = { Text("移动到其他歌单") },
                                onClick = {
                                    showMenu = false
                                    onMoveClick()
                                },
                                leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("从歌单中移除") },
                            onClick = {
                                showMenu = false
                                onRemoveClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("下一首播放") },
                            onClick = { showMenu = false },
                            leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "取消收藏" else "收藏") },
                            onClick = {
                                showMenu = false
                                onFavoriteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
