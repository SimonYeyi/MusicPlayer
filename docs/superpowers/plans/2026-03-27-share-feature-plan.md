# 分享功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在歌曲列表长按菜单和播放详情页添加分享功能，用户可以分享歌曲信息及音频文件给其他应用。

**Architecture:** ViewModel 通过 SharedFlow 发送分享事件，MainActivity 观察事件并调用 ShareHelper 执行分享。UI 层通过回调触发分享事件。

**Tech Stack:** Android Intent (ACTION_SEND), Kotlin Coroutines/Flow, Jetpack Compose

**Design Spec:** `docs/superpowers/specs/2026-03-27-share-feature-design.md`

---

## 文件变更概览

| 文件 | 变更类型 |
|------|----------|
| `util/ShareHelper.kt` | 新建 |
| `MusicViewModel.kt` | 修改 |
| `MainActivity.kt` | 修改 |
| `MyMusicScreen.kt` | 修改 |
| `HomeScreen.kt` | 修改 |
| `PlayDetailScreen.kt` | 修改 |

---

## Task 1: 创建 ShareHelper.kt

**Files:**
- Create: `app/src/main/java/com/musicplayer/util/ShareHelper.kt`

- [ ] **Step 1: 创建 ShareHelper.kt**

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

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/musicplayer/util/ShareHelper.kt
git commit -m "feat: 创建 ShareHelper 分享工具类"
```

---

## Task 2: MusicViewModel 添加分享状态

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/viewmodel/MusicViewModel.kt`

**添加位置:** 在 `ringtoneToastMessage` 下方添加

- [ ] **Step 1: 添加分享事件状态**

在 line 91 (`val ringtoneToastMessage`) 下方添加:

```kotlin
// 分享事件
private val _shareSongEvent = MutableSharedFlow<Song>()
val shareSongEvent: SharedFlow<Song> = _shareSongEvent.asSharedFlow()
```

- [ ] **Step 2: 添加 onShareClick 方法**

在 `onDismissRingtoneDialog` 方法下方添加:

```kotlin
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

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/musicplayer/presentation/viewmodel/MusicViewModel.kt
git commit -m "feat(MusicViewModel): 添加分享状态和 onShareClick 方法"
```

---

## Task 3: MainActivity 监听分享事件

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt`

- [ ] **Step 1: 添加 import**

在 import 区域添加:

```kotlin
import com.musicplayer.util.ShareHelper
```

- [ ] **Step 2: 添加分享事件监听**

在 `LaunchedEffect(Unit) { viewModel.ringtoneToastMessage.collectLatest ... }` (line 125-129) 之后添加:

```kotlin
// 监听分享事件
LaunchedEffect(Unit) {
    viewModel.shareSongEvent.collectLatest { song ->
        if (!ShareHelper.shareSong(this@MainActivity, song)) {
            Toast.makeText(this@MainActivity, "分享失败", Toast.LENGTH_SHORT).show()
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/musicplayer/presentation/ui/MainActivity.kt
git commit -m "feat(MainActivity): 监听分享事件"
```

---

## Task 4: MyMusicScreen.kt 绑定分享菜单

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt`

**变更点:**
1. `SongListItem` 函数签名 (line 833) 添加 `onShareClick: (Song) -> Unit = {}`
2. 分享菜单项 onClick 绑定回调
3. `LocalMusicTab`、`FavoritesTab`、`RecentlyPlayedTab` 中的 SongListItem 调用添加 onShareClick 参数

- [ ] **Step 1: 修改 SongListItem 函数签名**

找到 `fun SongListItem(` (约 line 833)，在参数列表中添加:

```kotlin
onShareClick: (Song) -> Unit = {},
```

- [ ] **Step 2: 修改分享菜单项 onClick**

找到分享菜单项 (约 line 952-956):

```kotlin
DropdownMenuItem(
    text = { Text("分享") },
    onClick = { showMenu = false },
    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
)
```

改为:

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

- [ ] **Step 3: 更新 LocalMusicTab 中的 SongListItem 调用**

在 LocalMusicTab 函数中找到 SongListItem 调用 (约 line 459)，添加:

```kotlin
onShareClick = { s -> viewModel.onShareClick(s) },
```

需要先在 LocalMusicTab 函数签名中添加 viewModel 参数或通过 uiState 访问。检查现有代码结构，使用合适的方式传递回调。

- [ ] **Step 4: 更新 FavoritesTab 和 RecentlyPlayedTab**

同样在 FavoritesTab 和 RecentlyPlayedTab 中的 SongListItem 调用添加 onShareClick 参数。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/mymusic/MyMusicScreen.kt
git commit -m "feat(MyMusicScreen): 绑定分享菜单"
```

---

## Task 5: HomeScreen.kt 绑定分享菜单

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/home/HomeScreen.kt`

**变更点:**
1. `HotSongItem` 函数签名 (line 373) 添加 `onShareClick: (Song) -> Unit = {}`
2. 分享菜单项 onClick 绑定回调

- [ ] **Step 1: 修改 HotSongItem 函数签名**

在 line 378 附近，`HotSongItem` 函数签名中添加:

```kotlin
onShareClick: (Song) -> Unit = {},
```

- [ ] **Step 2: 修改分享菜单项 onClick**

找到 `HotSongItem` 中的分享菜单项 (line 449-453):

```kotlin
DropdownMenuItem(
    text = { Text("分享") },
    onClick = { showMenu = false },
    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
)
```

改为:

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

- [ ] **Step 3: 更新 HomeScreen 中 HotSongItem 的调用**

在 HomeScreen.kt 中找到所有 `HotSongItem` 调用，添加 `onShareClick` 参数。

查看 HomeScreen.kt 中 HotSongItem 的调用位置 (约 line 159)，添加:

```kotlin
onShareClick = { s -> viewModel.onShareClick(s) },
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/home/HomeScreen.kt
git commit -m "feat(HomeScreen): 绑定分享菜单"
```

---

## Task 6: PlayDetailScreen.kt 添加分享功能

**Files:**
- Modify: `app/src/main/java/com/musicplayer/presentation/ui/screens/playdetail/PlayDetailScreen.kt`

**变更点:**
1. 更多菜单 (DropdownMenu) 中新增"分享"菜单项
2. TopAppBar 中添加分享按钮

- [ ] **Step 1: 在更多菜单中添加分享菜单项**

在 PlayDetailScreen.kt 中找到更多菜单 (DropdownMenu)，在"收藏"菜单项之后添加:

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

- [ ] **Step 2: 在 TopAppBar 中添加分享按钮**

找到 TopAppBar 的 actions 区域，在 IconButton 附近添加:

```kotlin
IconButton(onClick = { currentSong?.let { viewModel.onShareClick(it) } }) {
    Icon(Icons.Default.Share, contentDescription = "分享")
}
```

具体位置需要根据现有 TopAppBar 结构决定，可能在返回按钮或更多菜单按钮附近。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/musicplayer/presentation/ui/screens/playdetail/PlayDetailScreen.kt
git commit -m "feat(PlayDetailScreen): 添加分享功能"
```

---

## Task 7: 编写单元测试

**Files:**
- Create: `app/src/test/java/com/musicplayer/util/ShareHelperTest.kt`
- Modify: `app/src/test/java/com/musicplayer/presentation/viewmodel/MusicViewModelTest.kt`

- [ ] **Step 1: 创建 ShareHelperTest.kt**

```kotlin
package com.musicplayer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.musicplayer.domain.model.Song
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ShareHelperTest {

    private lateinit var context: Context
    private lateinit var song: Song

    @Before
    fun setup() {
        context = mock(Context::class.java)
        song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 180000L,
            uri = Uri.parse("content://media/audio/1"),
            albumArtUri = null
        )
    }

    @Test
    fun `shareSong with valid URI returns true`() {
        val result = ShareHelper.shareSong(context, song)
        assertTrue(result)
    }

    @Test
    fun `shareSong with null URI returns false`() {
        val songWithNullUri = song.copy(uri = null)
        val result = ShareHelper.shareSong(context, songWithNullUri)
        assertFalse(result)
    }
}
```

- [ ] **Step 2: 在 MusicViewModelTest 中添加 onShareClick 测试**

打开 `app/src/test/java/com/musicplayer/presentation/viewmodel/MusicViewModelTest.kt`，添加测试用例。

- [ ] **Step 3: 运行测试**

```bash
./gradlew testDebugUnitTest --tests "com.musicplayer.util.ShareHelperTest"
./gradlew testDebugUnitTest --tests "com.musicplayer.presentation.viewmodel.MusicViewModelTest"
```

- [ ] **Step 4: Commit**

```bash
git add app/src/test/java/com/musicplayer/util/ShareHelperTest.kt
git add app/src/test/java/com/musicplayer/presentation/viewmodel/MusicViewModelTest.kt
git commit -m "test: 添加分享功能单元测试"
```

---

## Task 8: 构建验证

- [ ] **Step 1: 运行所有测试**

```bash
./gradlew test
```

- [ ] **Step 2: 构建 debug APK**

```bash
./gradlew assembleDebug
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: 完成分享功能"
```

---

## 注意事项

1. **MyMusicScreen.kt 的 SongListItem 回调传递**: LocalMusicTab/FavoritesTab/RecentlyPlayedTab 需要通过 viewModel 访问 onShareClick。由于这些是 Tab Composable 函数而非 Screen，需确认如何获取 viewModel 实例（可能是通过参数传递或使用 hiltViewModel）。

2. **PlayDetailScreen.kt TopAppBar 按钮位置**: 需要根据现有 TopAppBar 布局决定具体插入位置，确保视觉一致性。

3. **HotSongItem 在 HomeScreen.kt 中的调用**: 需要在 viewModel 可访问的位置添加 onShareClick 回调。
