# 设为铃声功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在歌曲列表项长按菜单中添加"设为铃声"功能，支持来电铃声、通知铃声、闹钟铃声

**Architecture:** 铃声核心逻辑放在 `RingtoneHelper` 工具类，ViewModel 处理权限流程和弹框状态，UI 组件通过回调触发功能

**Tech Stack:** Android RingtoneManager, Settings.System.canWrite, Compose AlertDialog

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `util/RingtoneHelper.kt` | 铃声核心逻辑：权限检查、设置铃声 |
| `MusicViewModel.kt` | 铃声相关状态和回调 |
| `MyMusicScreen.kt` | 传递 `onSetRingtoneClick` 到 SongListItem |
| `HomeScreen.kt` | 传递 `onSetRingtoneClick` 到 SongListItem |
| `PlaylistScreen.kt` | 传递 `onSetRingtoneClick` 到 SongListItem |
| `PlayDetailScreen.kt` | 传递 `onSetRingtoneClick` 到 SongListItem |

---

## Task 1: 创建 RingtoneHelper 工具类

**Files:**
- Create: `app/src/main/java/com/musicplayer/util/RingtoneHelper.kt`

- [ ] **Step 1: 创建文件**

```kotlin
package com.musicplayer.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.musicplayer.R

object RingtoneHelper {

    enum class RingtoneType(val title: String, val settingName: String) {
        RINGTONE("来电铃声", Settings.System.RINGTONE),
        NOTIFICATION("通知铃声", Settings.System.NOTIFICATION_SOUND),
        ALARM("闹钟铃声", Settings.System.ALARM_ALERT)
    }

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun setRingtone(context: Context, songUri: Uri, type: RingtoneType): Boolean {
        return try {
            if (canWriteSettings(context)) {
                // 直接设置铃声
                val values = ContentValues().apply {
                    put(Settings.System.RINGTONE, songUri.toString())
                }
                context.contentResolver.insert(Settings.System.getUriFor(type.settingName), values)?.let {
                    context.contentResolver.notifyChange(it, null)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showRingtonePicker(context: Context, type: RingtoneType, songUri: Uri) {
        try {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, when (type) {
                    RingtoneType.RINGTONE -> RingtoneManager.TYPE_RINGTONE
                    RingtoneType.NOTIFICATION -> RingtoneManager.TYPE_NOTIFICATION
                    RingtoneType.ALARM -> RingtoneManager.TYPE_ALARM
                })
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设为${type.title}")
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, songUri)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "设置铃声失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun getRingtoneTypeIntent(context: Context, type: RingtoneType): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/musicplayer/util/RingtoneHelper.kt
git commit -m "feat: 添加 RingtoneHelper 工具类"
```

---

## Task 2: 修改 MusicViewModel 添加铃声相关状态

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 添加 import**

在 import 区添加：
```kotlin
import com.musicplayer.util.RingtoneHelper
import android.provider.Settings
import android.content.Intent
import android.net.Uri
```

- [ ] **Step 2: 添加状态**

在 `MusicViewModel` 类中添加：

```kotlin
// 铃声类型选择弹框
val showRingtoneTypeDialog = MutableStateFlow(false)
val pendingRingtoneSongId = MutableStateFlow<Long?>(null)

// 权限解释弹框
val showRingtonePermissionDialog = MutableStateFlow(false)

// 铃声设置结果 Toast
val ringtoneToastMessage = MutableSharedFlow<String>()
```

- [ ] **Step 3: 添加方法**

在 MusicViewModel 类中添加：

```kotlin
fun onSetRingtoneClick(songId: Long) {
    if (RingtoneHelper.canWriteSettings(application)) {
        pendingRingtoneSongId.value = songId
        showRingtoneTypeDialog.value = true
    } else {
        showRingtonePermissionDialog.value = true
    }
}

fun onRingtoneTypeSelected(type: RingtoneHelper.RingtoneType) {
    val songId = pendingRingtoneSongId.value ?: return
    viewModelScope.launch {
        val song = musicRepository.getSongById(songId)
        if (song != null) {
            val success = RingtoneHelper.setRingtone(application, song.uri, type)
            if (success) {
                ringtoneToastMessage.emit("已设为${type.title}")
            } else {
                // 直接设置失败，尝试用系统选择器
                RingtoneHelper.showRingtonePicker(application, type, song.uri)
            }
        }
        showRingtoneTypeDialog.value = false
        pendingRingtoneSongId.value = null
    }
}

fun onDismissRingtoneDialog() {
    showRingtoneTypeDialog.value = false
    showRingtonePermissionDialog.value = false
    pendingRingtoneSongId.value = null
}

fun onGoToSettingsForRingtone() {
    showRingtonePermissionDialog.value = false
    val intent = RingtoneHelper.getRingtoneTypeIntent(application, RingtoneHelper.RingtoneType.RINGTONE)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    application.startActivity(intent)
}

fun onReturnFromSettingsForRingtone() {
    // Activity 在 onResume 时调用此方法检查权限
    if (RingtoneHelper.canWriteSettings(application)) {
        val songId = pendingRingtoneSongId.value
        if (songId != null) {
            pendingRingtoneSongId.value = null
            onSetRingtoneClick(songId)
        }
    } else {
        pendingRingtoneSongId.value = null
        viewModelScope.launch {
            ringtoneToastMessage.emit("无权限，无法设置铃声")
        }
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/viewmodel/MusicViewModel.kt
git commit -m "feat: MusicViewModel 添加铃声功能状态和方法"
```

---

## Task 3: 修改 SongListItem 添加铃声回调

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt:822-960`

- [ ] **Step 1: 添加参数**

在 `SongListItem` 函数签名中添加：

```kotlin
@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isCurrentlyPlaying: Boolean = false,
    onRemoveFromRecentClick: (() -> Unit)? = null,
    onRemoveFromMusicClick: (() -> Unit)? = null,
    onSetRingtoneClick: (() -> Unit)? = null,  // 新增
    modifier: Modifier = Modifier
)
```

- [ ] **Step 2: 修改菜单点击逻辑**

找到"设为铃声"的 DropdownMenuItem，修改 onClick：

```kotlin
DropdownMenuItem(
    text = { Text("设为铃声") },
    onClick = {
        showMenu = false
        onSetRingtoneClick?.invoke()
    },
    leadingIcon = { Icon(Icons.Default.RingVolume, contentDescription = null) }
)
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt
git commit -m "feat: SongListItem 添加设为铃声回调"
```

---

## Task 4: 修改 MyMusicScreen 传递铃声回调

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt`

- [ ] **Step 1: 找到 LocalSongsContent 函数**

添加 `onSetRingtoneClick` 参数到函数签名和调用处。

- [ ] **Step 2: 找到 FavoriteSongsContent 函数**

同样添加传递。

- [ ] **Step 3: 找到 RecentPlaysContent 函数**

同样添加传递。

- [ ] **Step 4: 在 MyMusicScreen 主Composable中找到 SongListItem 调用处**

在所有 `SongListItem` 调用处添加 `onSetRingtoneClick = { viewModel.onSetRingtoneClick(song.id) }`

- [ ] **Step 5: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt
git commit -m "feat: MyMusicScreen 传递设为铃声回调"
```

---

## Task 5: 修改 HomeScreen 传递铃声回调

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/home/HomeScreen.kt`

- [ ] **Step 1: 找到所有 SongListItem 调用处**

添加 `onSetRingtoneClick = { viewModel.onSetRingtoneClick(song.id) }`

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/home/HomeScreen.kt
git commit -m "feat: HomeScreen 传递设为铃声回调"
```

---

## Task 6: 修改 PlaylistScreen 传递铃声回调

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/playlist/PlaylistScreen.kt`

- [ ] **Step 1: 找到所有 SongListItem 调用处**

添加 `onSetRingtoneClick = { viewModel.onSetRingtoneClick(song.id) }`

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/playlist/PlaylistScreen.kt
git commit -m "feat: PlaylistScreen 传递设为铃声回调"
```

---

## Task 7: 修改 PlayDetailScreen 传递铃声回调

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/playdetail/PlayDetailScreen.kt`

- [ ] **Step 1: 找到 PlaylistPickerDialog 调用**

PlayDetailScreen 使用 PlaylistPickerDialog 添加到歌单，需要添加铃声功能。

找到播放详情页中的歌曲列表或菜单项，添加铃声选项。

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/playdetail/PlayDetailScreen.kt
git commit -m "feat: PlayDetailScreen 添加设为铃声功能"
```

---

## Task 8: 在 MainActivity 中观察铃声 Toast 和处理权限返回

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt`

- [ ] **Step 1: 收集 ringtoneToastMessage**

在 Activity 中 launchWhenStarted 或类似方式收集 `viewModel.ringtoneToastMessage`，显示 Toast。

- [ ] **Step 2: 在 onResume 中调用 onReturnFromSettingsForRingtone**

```kotlin
override fun onResume() {
    super.onResume()
    // 检查铃声权限返回
    if (viewModel.showRingtonePermissionDialog.value ||
        viewModel.pendingRingtoneSongId.value != null) {
        viewModel.onReturnFromSettingsForRingtone()
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt
git commit -m "feat: MainActivity 处理铃声 Toast 和权限返回"
```

---

## Task 9: 添加铃声选择对话框和权限解释对话框 UI

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt`

- [ ] **Step 1: 添加铃声类型选择对话框**

在 MainActivity 的 Compose UI 中添加：

```kotlin
if (viewModel.showRingtoneTypeDialog.collectAsState().value) {
    AlertDialog(
        onDismissRequest = { viewModel.onDismissRingtoneDialog() },
        title = { Text("设为铃声") },
        text = {
            Column {
                RingtoneHelper.RingtoneType.entries.forEach { type ->
                    ListItem(
                        headlineContent = { Text(type.title) },
                        modifier = Modifier.clickable {
                            viewModel.onRingtoneTypeSelected(type)
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { viewModel.onDismissRingtoneDialog() }) {
                Text("取消")
            }
        }
    )
}
```

- [ ] **Step 2: 添加权限解释对话框**

```kotlin
if (viewModel.showRingtonePermissionDialog.collectAsState().value) {
    AlertDialog(
        onDismissRequest = { viewModel.onDismissRingtoneDialog() },
        title = { Text("提示") },
        text = { Text("设置铃声需要特殊权限，请在设置中授权后重试。") },
        confirmButton = {
            TextButton(onClick = { viewModel.onGoToSettingsForRingtone() }) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onDismissRingtoneDialog() }) {
                Text("取消")
            }
        }
    )
}
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt
git commit -m "feat: MainActivity 添加铃声选择和权限对话框"
```

---

## Task 10: 构建并测试

- [ ] **Step 1: 构建调试版本**

```bash
./gradlew assembleDebug
```

- [ ] **Step 2: 安装并测试**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

测试场景：
1. 本地音乐列表长按歌曲 -> 选择"设为铃声" -> 选择来电铃声 -> 验证 Toast
2. 收藏列表同上
3. 最近播放列表同上
4. 歌单列表同上
5. 播放详情页同上
6. 无权限时点击"设为铃声" -> 验证弹框 -> 点击"去设置" -> 验证跳转设置页
7. 从设置页返回 -> 验证 Toast 提示无权限

- [ ] **Step 3: Commit**
```bash
git add -A
git commit -m "feat: 完成设为铃声功能"
```
