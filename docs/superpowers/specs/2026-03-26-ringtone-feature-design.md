# 设为铃声功能设计

## 1. 概述

在歌曲列表项的长按菜单中添加"设为铃声"功能，支持将歌曲设为来电铃声、通知铃声或闹钟铃声。

## 2. 核心逻辑 - RingtoneHelper

### 文件位置
`app/src/main/java/com/musicplayer/utils/RingtoneHelper.kt`

### 功能
```kotlin
object RingtoneHelper {
    fun canWriteSettings(context: Context): Boolean
    fun setRingtone(context: Context, songUri: Uri, type: RingtoneType): Boolean
    enum class RingtoneType { RINGTONE, NOTIFICATION, ALARM }
}
```

### 实现要点
- `canWriteSettings()` 使用 `Settings.System.canWrite(context)` 检查权限
- `setRingtone()` 使用 `RingtoneManager` 设置铃声
  - 先尝试 `setRingtone()` 方法
  - 如果失败，改用 `RingtoneManager.ACTION_RINGTONE_PICKER` 打开系统选择器让用户确认

## 3. 权限流程

每次点击"设为铃声"：

```
开始
  ↓
检查 canWriteSettings()
  ↓
权限检查结果
  ├── 有权 ──────────────────────────────────────┐
  │    ↓                                         │
  │   弹出铃声类型选择对话框                       │
  │   (来电/通知/闹钟)                             │
  │    ↓                                         │
  │   用户选择类型                               │
  │    ↓                                         │
  │   调用 setRingtone()                         │
  │    ↓                                         │
  │   成功 → Toast "已设为XXX"                   │
  │   失败 → Toast "设置失败"                    │
  │    ↓                                         │
  └── 无权 ──────────────────────────────────────┐
       ↓                                         │
      弹出解释对话框                              │
      "需要权限才能设置铃声，是否前往设置？"        │
       ↓                                         │
      用户点"取消" → 结束                         │
       ↓                                         │
      用户点"去设置" → 跳转系统设置页面           │
       ↓                                         │
      用户从设置返回                              │
       ↓                                         │
      检查权限                                    │
       ↓                                         │
      仍有权限 ──→ 继续铃声类型选择流程            │
       ↓                                         │
      无权限 → Toast "无权限，无法设置铃声"       │
```

## 4. UI 设计

### 4.1 铃声类型选择对话框
```
┌─────────────────────────────┐
│        设为铃声             │
├─────────────────────────────┤
│  📞 来电铃声                │
│  🔔 通知铃声                │
│  ⏰ 闹钟铃声                │
├─────────────────────────────┤
│              [取消]         │
└─────────────────────────────┘
```

### 4.2 权限解释对话框
```
┌─────────────────────────────┐
│        提示                 │
├─────────────────────────────┤
│  设置铃声需要特殊权限。      │
│  请在设置中授权后重试。      │
├─────────────────────────────┤
│        [取消]  [去设置]      │
└─────────────────────────────┘
```

## 5. ViewModel 变更

### MusicViewModel 新增
```kotlin
// 铃声类型选择弹框
val showRingtoneTypeDialog = MutableStateFlow(false)
val pendingRingtoneSongId = MutableStateFlow<Long?>(null)

// 权限解释弹框
val showRingtonePermissionDialog = MutableStateFlow(false)

// 方法
fun onSetRingtoneClick(songId: Long)
fun onRingtoneTypeSelected(type: RingtoneHelper.RingtoneType)
fun onDismissRingtoneDialog()
fun onGoToSettingsForRingtone()
```

## 6. SongListItem 变更

### 新增参数
```kotlin
@Composable
fun SongListItem(
    ...
    onSetRingtoneClick: (() -> Unit)? = null,  // 新增
    ...
)
```

### 菜单渲染
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

## 7. 修改文件清单

| 文件 | 变更 |
|------|------|
| `utils/RingtoneHelper.kt` | 新建 |
| `MusicViewModel.kt` | 添加铃声相关状态和方法 |
| `MyMusicScreen.kt` | 传递 `onSetRingtoneClick` 回调 |
| `HomeScreen.kt` | 传递 `onSetRingtoneClick` 回调 |
| `PlaylistScreen.kt` | 传递 `onSetRingtoneClick` 回调 |
| `PlayDetailScreen.kt` | 传递 `onSetRingtoneClick` 回调 |

## 8. 成功/失败提示

- 设置成功：Toast "已设为来电铃声" / "已设为通知铃声" / "已设为闹钟铃声"
- 设置失败：Toast "设置铃声失败"
- 无权限：Toast "无权限，无法设置铃声"
