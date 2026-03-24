package com.musicplayer.presentation.ui.screens.playdetail

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.musicplayer.R
import com.musicplayer.domain.model.PlayMode
import com.musicplayer.domain.model.Song
import com.musicplayer.presentation.ui.components.formatDuration
import com.musicplayer.presentation.viewmodel.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayDetailScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playbackState = uiState.playbackState
    val currentSong = playbackState.currentSong
    val scope = rememberCoroutineScope()

    // --- 进度条逻辑修复 ---
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    val displaySliderPosition = if (isDragging) dragPosition else {
        if (playbackState.duration > 0) playbackState.currentPosition.toFloat() / playbackState.duration.toFloat() else 0f
    }

    // --- 弹窗状态 ---
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showQueueSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 封面旋转动画
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "album_rotation"
    )

    val playButtonScale by animateFloatAsState(
        targetValue = if (playbackState.isPlaying) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "play_button_scale"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
            ) {
                // --- Top Bar ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.now_playing).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = currentSong?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 封面 (Album Art) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentSong?.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.9f)
                            .clip(CircleShape)
                            .rotate(if (playbackState.isPlaying) rotation else 0f)
                            .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentScale = ContentScale.Crop,
                        error = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(100.dp))
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 歌曲信息 ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentSong?.title ?: stringResource(R.string.no_song_playing),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong?.artist ?: stringResource(R.string.unknown_artist),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 进度条 (Slider) ---
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Slider(
                        value = displaySliderPosition,
                        onValueChange = {
                            isDragging = true
                            dragPosition = it
                        },
                        onValueChangeFinished = {
                            val newPosition = (dragPosition * playbackState.duration).toLong()
                            viewModel.seekTo(newPosition)
                            scope.launch {
                                delay(100) // 解决同步回弹问题
                                isDragging = false
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentMs = if (isDragging) (dragPosition * playbackState.duration).toLong() else playbackState.currentPosition
                        Text(formatDuration(currentMs), style = MaterialTheme.typography.bodySmall)
                        Text(formatDuration(playbackState.duration), style = MaterialTheme.typography.bodySmall)
                    }
                }

                // --- 控制按钮 ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 播放模式
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            imageVector = when (playbackState.playMode) {
                                PlayMode.SHUFFLE -> Icons.Default.Shuffle
                                PlayMode.ONE_LOOP -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = null,
                            tint = if (playbackState.playMode == PlayMode.OFF)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.primary
                        )
                    }

                    // 上一曲 - 播放/暂停 - 下一曲
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = { viewModel.playPrevious() }) {
                            Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(48.dp))
                        }

                        FloatingActionButton(
                            onClick = { viewModel.togglePlayPause() },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.scale(playButtonScale).size(72.dp)
                        ) {
                            Icon(
                                if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                null,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        IconButton(onClick = { viewModel.playNext() }) {
                            Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(48.dp))
                        }
                    }

                    // 播放队列按钮
                    IconButton(onClick = { showQueueSheet = true }) {
                        Icon(Icons.Default.QueueMusic, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- 播放队列弹窗 ---
    if (showQueueSheet) {
        QueueBottomSheet(
            sheetState = sheetState,
            playlist = uiState.currentPlaylist,
            currentSongId = currentSong?.id,
            onDismiss = { showQueueSheet = false },
            onSongClick = { _, index ->
                viewModel.playSongAtIndex(index)
                scope.launch {
                    sheetState.hide()
                    showQueueSheet = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueBottomSheet(
    sheetState: SheetState,
    playlist: List<Song>,
    currentSongId: Long?,
    onDismiss: () -> Unit,
    onSongClick: (Song, Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text(
                "播放队列 (${playlist.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                itemsIndexed(
                    items = playlist,
                    key = { _, song -> song.id }
                ) { index, song ->
                    val isCurrent = song.id == currentSongId
                    ListItem(
                        headlineContent = {
                            Text(
                                song.title,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = { Text(song.artist, maxLines = 1) },
                        leadingContent = {
                            if (isCurrent) {
                                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("${index + 1}", modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                            }
                        },
                        modifier = Modifier.clickable { onSongClick(song, index) }
                    )
                }
            }
        }
    }
}