package com.sm.musicplayer.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sm.musicplayer.ui.theme.MoodTheme
import com.sm.musicplayer.ui.theme.getMoodColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentMoodTheme: MoodTheme,
    isDarkMode: Boolean,
    onMoodThemeChange: (MoodTheme) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onClearCache: () -> Unit,
    onAboutClick: () -> Unit,
    cacheCleared: Boolean = false
) {
    var showMoodDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(cacheCleared) {
        if (cacheCleared) {
            snackbarHostState.showSnackbar("缓存已清除")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSection(title = "外观")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Palette,
                    title = "心情主题",
                    subtitle = currentMoodTheme.displayName,
                    onClick = { showMoodDialog = true }
                )
            }
            
            item {
                SettingsItem(
                    icon = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    title = "深色模式",
                    subtitle = if (isDarkMode) "已开启" else "已关闭",
                    trailing = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeChange
                        )
                    },
                    onClick = { onDarkModeChange(!isDarkMode) }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSection(title = "播放")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Shuffle,
                    title = "播放模式",
                    subtitle = "顺序播放",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Equalizer,
                    title = "音质设置",
                    subtitle = "标准",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Speaker,
                    title = "音量调节",
                    subtitle = "媒体音量",
                    onClick = { }
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSection(title = "存储")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Refresh,
                    title = "刷新音乐库",
                    subtitle = "重新扫描本地音乐",
                    onClick = { }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.DeleteSweep,
                    title = "清除缓存",
                    subtitle = "清除专辑封面等缓存",
                    onClick = onClearCache
                )
            }
            
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSection(title = "关于")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "关于应用",
                    subtitle = "版本 1.0",
                    onClick = onAboutClick
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Filled.Code,
                    title = "开源许可",
                    subtitle = "查看开源库许可证",
                    onClick = { }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    if (showMoodDialog) {
        MoodThemeDialog(
            currentTheme = currentMoodTheme,
            onThemeSelected = {
                onMoodThemeChange(it)
                showMoodDialog = false
            },
            onDismiss = { showMoodDialog = false }
        )
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = trailing,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun MoodThemeDialog(
    currentTheme: MoodTheme,
    onThemeSelected: (MoodTheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择心情主题") },
        text = {
            LazyColumn {
                items(MoodTheme.entries.toList()) { theme ->
                    MoodThemeItem(
                        theme = theme,
                        isSelected = theme == currentTheme,
                        onClick = { onThemeSelected(theme) }
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

@Composable
fun MoodThemeItem(
    theme: MoodTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = getMoodColors(theme)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.primary)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = colors.primary
            )
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "音乐播放器",
                style = MaterialTheme.typography.headlineSmall
            ) 
        },
        text = {
            Column {
                Text(
                    text = "版本 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "一款简洁美观的本地音乐播放器，支持播放控制、播放列表管理、心情主题等功能。",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "© 2024 SM Music",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
