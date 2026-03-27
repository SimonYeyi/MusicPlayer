# 分享功能设计

## 1. 技术视角

### 1.1 概述

在歌曲列表长按菜单和播放详情页添加分享功能，用户可以分享歌曲信息及音频文件给其他应用。

**分享内容**：文本「分享歌曲：{歌曲名} - {艺术家}」+ 音频文件（audio/*）
**失败处理**：音频分享失败时显示 Toast 提示，不自动降级

### 1.2 核心逻辑 - ShareHelper

文件位置：`app/src/main/java/com/musicplayer/util/ShareHelper.kt`

```kotlin
package com.musicplayer.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.musicplayer.domain.model.Song

object ShareHelper {

    fun shareSong(context: Context, song: Song): Boolean {
        val uri = song.uri ?: return false

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_TEXT, "分享歌曲：${song.title} - ${song.artist}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(Intent.createChooser(intent, "分享歌曲"))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
```

### 1.3 实现要点

- `Intent.ACTION_SEND` 用于分享
- `type = "audio/*"` 表示分享音频文件
- `EXTRA_TEXT` 包含分享文案：`"分享歌曲：{title} - {artist}"`
- `EXTRA_STREAM` 包含音频文件 URI
- `FLAG_GRANT_READ_URI_PERMISSION` 授予临时读取权限
- `Intent.createChooser` 显示系统分享面板
- 失败返回 `false`，由调用方处理 Toast 提示

### 1.4 流程图

```
用户点击分享
    ↓
ViewModel.onShareClick(song)
    ↓
song.uri != null → emit(shareSongEvent)
    ↓
MainActivity 收到事件
    ↓
ShareHelper.shareSong()
    ↓
  ├── 成功 → 启动系统分享面板
  └── 失败 → Toast "分享失败"
```

### 1.5 ViewModel 变更

MusicViewModel 新增：

```kotlin
// 分享事件
private val _shareSongEvent = MutableSharedFlow<Song>()
val shareSongEvent: SharedFlow<Song> = _shareSongEvent.asSharedFlow()

fun onShareClick(song: Song) {
    viewModelScope.launch {
        if (song.uri != null) {
            _shareSongEvent.emit(song)
        } else {
            _ringtoneToastMessage.emit("分享失败")
        }
    }
}
```

### 1.6 UI 变更

#### 1.6.1 SongListItem 参数变更

SongListItem 函数签名新增 `onShareClick: (Song) -> Unit = {}` 参数。

分享菜单项修改：

```kotlin
DropdownMenuItem(
    text = { Text("分享") },
    onClick = {
        showMenu = false
        onShareClick(song)
    },
    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
)
```

#### 1.6.2 绑定位置

| Screen | 组件 | 变更 |
|--------|------|------|
| MyMusicScreen.kt | LocalMusicTab/FavoritesTab/RecentlyPlayedTab | SongListItem 添加 `onShareClick` 参数并绑定 |
| HomeScreen.kt | HotSongItem | HotSongItem 添加 `onShareClick` 参数，分享菜单项绑定回调 |
| PlayDetailScreen.kt | 更多菜单（DropdownMenu） | 新增"分享"菜单项 |
| PlayDetailScreen.kt | TopAppBar | 新增 IconButton 分享按钮 |

#### 1.6.3 PlayDetailScreen 更多菜单新增分享项

在 PlayDetailScreen 的更多菜单（DropdownMenu）中新增分享菜单项：

```kotlin
DropdownMenuItem(
    text = { Text("分享") },
    onClick = {
        showMenu = false
        currentSong?.let { viewModel.onShareClick(it) }
    },
    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
)
```

#### 1.6.4 PlayDetailScreen TopAppBar 分享按钮

在 PlayDetailScreen 顶部工具栏添加分享按钮：

```kotlin
IconButton(onClick = { currentSong?.let { viewModel.onShareClick(it) } }) {
    Icon(Icons.Default.Share, contentDescription = "分享")
}
```

### 1.7 MainActivity 监听

```kotlin
import com.musicplayer.util.ShareHelper
import kotlinx.coroutines.flow.collectLatest

// 在 setContent 的 LaunchedEffect 中添加
LaunchedEffect(Unit) {
    viewModel.shareSongEvent.collectLatest { song ->
        if (!ShareHelper.shareSong(this@MainActivity, song)) {
            Toast.makeText(this@MainActivity, "分享失败", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### 1.8 文件变更清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `util/ShareHelper.kt` | 新建 | 分享工具类 |
| `MusicViewModel.kt` | 修改 | 新增 shareSongEvent、onShareClick |
| `MainActivity.kt` | 修改 | 导入 ShareHelper，监听 shareSongEvent |
| `MyMusicScreen.kt` | 修改 | SongListItem 添加 onShareClick 参数，LocalMusicTab/FavoritesTab/RecentlyPlayedTab 绑定 |
| `HomeScreen.kt` | 修改 | HotSongItem 添加 onShareClick 参数，分享菜单项绑定回调 |
| `PlayDetailScreen.kt` | 修改 | 更多菜单新增"分享"菜单项，TopAppBar 新增分享按钮 |

### 1.9 测试要求

| 测试类 | 覆盖场景 |
|--------|----------|
| `ShareHelperTest.kt` | `shareSong()` 正常流程、URI 为 null 返回 false、ActivityNotFoundException 返回 false |
| `MusicViewModelTest.kt` | `onShareClick()` 正常触发事件、URI 为 null 时发送 Toast |

### 1.10 成功/失败提示

- 成功：启动系统分享面板（无 Toast）
- 失败：Toast "分享失败"
