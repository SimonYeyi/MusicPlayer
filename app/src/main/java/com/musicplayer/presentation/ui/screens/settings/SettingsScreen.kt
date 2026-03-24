package com.musicplayer.presentation.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.musicplayer.R
import com.musicplayer.domain.model.MoodTheme
import com.musicplayer.presentation.ui.components.MoodThemePickerDialog
import com.musicplayer.presentation.viewmodel.MusicViewModel

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val showArrow: Boolean = true,
    val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRescanDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 主题设置
            item {
                SettingsSection(title = stringResource(R.string.mood_theme)) {
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.Palette,
                            title = stringResource(R.string.select_mood),
                            subtitle = uiState.currentTheme.displayName,
                            onClick = { viewModel.toggleThemePicker() }
                        )
                    )
                }
            }

            // 播放设置
            item {
                SettingsSection(title = stringResource(R.string.playback_settings)) {
                    SettingsItem(
                        item = SettingsItem(
                            icon = when (uiState.playbackState.playMode) {
                                com.musicplayer.domain.model.PlayMode.SHUFFLE -> Icons.Default.Shuffle
                                else -> Icons.Default.Repeat
                            },
                            title = "播放模式",
                            subtitle = when (uiState.playbackState.playMode) {
                                com.musicplayer.domain.model.PlayMode.OFF -> "关闭"
                                com.musicplayer.domain.model.PlayMode.LIST_LOOP -> "列表循环"
                                com.musicplayer.domain.model.PlayMode.SHUFFLE -> "随机播放"
                                com.musicplayer.domain.model.PlayMode.ONE_LOOP -> "单曲循环"
                            },
                            showArrow = false,
                            onClick = { viewModel.toggleShuffle() }
                        )
                    )
                }
            }

            // 音乐管理
            item {
                SettingsSection(title = stringResource(R.string.music_management)) {
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.MusicNote,
                            title = stringResource(R.string.local_music),
                            subtitle = "${uiState.songs.size} ${stringResource(R.string.songs)}",
                            showArrow = true,
                            onClick = { onNavigateToTab(0) }
                        )
                    )
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.Refresh,
                            title = "重新扫描音乐",
                            subtitle = "从设备扫描音乐文件",
                            showArrow = false,
                            onClick = { showRescanDialog = true }
                        )
                    )
                }
            }

            // 播放历史
            item {
                SettingsSection(title = stringResource(R.string.history)) {
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.recently_played),
                            subtitle = "查看播放历史",
                            showArrow = true,
                            onClick = { onNavigateToTab(2) }
                        )
                    )
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.Favorite,
                            title = stringResource(R.string.favorites),
                            subtitle = "我收藏的歌曲",
                            showArrow = true,
                            onClick = { onNavigateToTab(1) }
                        )
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = stringResource(R.string.about)) {
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.Info,
                            title = "版本",
                            subtitle = "1.0.0",
                            showArrow = false
                        )
                    )
                    SettingsItem(
                        item = SettingsItem(
                            icon = Icons.Default.Code,
                            title = "开发者",
                            subtitle = "Claude Code",
                            showArrow = false
                        )
                    )
                }
            }
        }

        // 心情主题选择对话框
        if (uiState.showThemePicker) {
            MoodThemePickerDialog(
                currentTheme = uiState.currentTheme,
                onThemeSelected = { theme -> viewModel.setTheme(theme) },
                onDismiss = { viewModel.hideThemePicker() }
            )
        }

        // 重新扫描确认对话框
        if (showRescanDialog) {
            AlertDialog(
                onDismissRequest = { showRescanDialog = false },
                title = { Text("重新扫描音乐") },
                text = { Text("将重新扫描设备中的所有音乐文件。是否继续？") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.refreshMusic()
                            showRescanDialog = false
                        }
                    ) {
                        Text("扫描")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRescanDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    item: SettingsItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.onClick != null) Modifier.clickable(onClick = item.onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.showArrow) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
