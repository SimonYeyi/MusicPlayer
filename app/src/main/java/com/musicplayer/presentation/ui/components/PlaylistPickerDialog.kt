package com.musicplayer.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.musicplayer.data.local.PlaylistEntity
import kotlinx.coroutines.launch

/**
 * 统一的歌单选择器弹框，同时支持选择已有歌单和新建歌单。
 * - 列表顶部始终显示"新建歌单"选项
 * - 新建完成后自动关闭弹框并通过 [onPlaylistCreated] 回调通知调用方
 * - 选择已有歌单后通过 [onPlaylistSelected] 回调通知调用方
 * - 新建时通过 [checkDuplicate] 检查同名歌单并提示
 *
 * @param playlists 可选歌单列表（可为空）
 * @param title 弹框标题，默认"添加到歌单"
 * @param excludedPlaylistIds 排除在外的歌单 ID 集合（用于"移动到"场景）
 * @param onDismiss 弹框关闭回调
 * @param onPlaylistSelected 选择已有歌单时的回调，参数为歌单 ID
 * @param onPlaylistCreated 新建歌单时的回调，参数为歌单名称，返回新建的歌单 ID
 * @param checkDuplicate 检查歌单名称是否已存在，返回 true 表示已存在
 */
@Composable
fun PlaylistPickerDialog(
    playlists: List<PlaylistEntity>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (playlistId: Long) -> Unit,
    onPlaylistCreated: suspend (name: String) -> Long,
    title: String = "添加到歌单",
    excludedPlaylistIds: Set<Long> = emptySet(),
    checkDuplicate: suspend (String) -> Boolean = { false },
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showCreateDialog) {
        CreatePlaylistDialogInternal(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                scope.launch {
                    val newId = onPlaylistCreated(name)
                    onDismiss()
                }
            },
            checkDuplicate = checkDuplicate
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = {
                LazyColumn {
                    // 最上方：新建歌单
                    item {
                        ListItem(
                            headlineContent = { Text("新建歌单") },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            modifier = Modifier.clickable { showCreateDialog = true }
                        )
                    }
                    // 已有歌单列表
                    items(playlists.filter { it.id !in excludedPlaylistIds }) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            supportingContent = { Text("${playlist.songCount} 首歌曲") },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                onPlaylistSelected(playlist.id)
                                onDismiss()
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CreatePlaylistDialogInternal(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    checkDuplicate: suspend (String) -> Boolean = { false },
) {
    var playlistName by remember { mutableStateOf("") }
    var isDuplicate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusManager.clearFocus()
        keyboardController?.show()
    }

    val confirmEnabled = playlistName.isNotBlank() && !isDuplicate

    fun handleCreate(name: String) {
        if (name.isBlank()) return
        scope.launch {
            if (checkDuplicate(name)) {
                isDuplicate = true
            } else {
                onCreate(name)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建歌单") },
        text = {
            Column {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = {
                        playlistName = it
                        isDuplicate = false
                    },
                    label = { Text("歌单名称") },
                    isError = isDuplicate,
                    supportingText = if (isDuplicate) {
                        { Text("歌单名称已存在") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { handleCreate(playlistName) }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { handleCreate(playlistName) },
                enabled = confirmEnabled
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
