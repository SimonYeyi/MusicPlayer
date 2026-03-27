# 音乐播放核心功能设计

## 1. 概述

提供完整的音乐播放核心能力，包括音乐扫描、播放控制、后台播放、通知栏控制、随机播放和音频设备切换。

## 2. 核心逻辑

### 2.1 数据模型

**Song.kt** - 歌曲领域模型
```kotlin
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumArtUri: Uri?,
    val isHot: Boolean = false
)
```

**PlaybackState.kt** - 播放状态
```kotlin
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playMode: PlayMode = PlayMode.LIST_LOOP
)

enum class PlayMode {
    OFF,           // 关闭（播放完停止）
    LIST_LOOP,     // 列表循环
    SHUFFLE,       // 随机播放
    ONE_LOOP       // 单曲循环
}
```

**SongEntity.kt** - 数据库实体
```kotlin
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: String,
    val albumArtUri: String?
)
```

### 2.2 音乐扫描逻辑

使用 MediaStore API 扫描设备音乐：

```kotlin
suspend fun scanMusicFromDevice() {
    // 1. 清空旧数据
    songDao.clearAll()
    // 2. 查询 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    // 3. 过滤：IS_MUSIC != 0
    // 4. 字段：_ID, TITLE, ARTIST, ALBUM, DURATION, ALBUM_ID
    // 5. 排序：TITLE ASC
    // 6. 插入新数据
}
```

- albumArtUri 优先使用 `ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId)`
- 专辑封面通过 MediaMetadataRetriever 从音频文件提取

### 2.3 播放服务架构

**MusicPlaybackService** - Foreground Service

```
MusicPlaybackService
├── mediaPlayer: MediaPlayer          # 音频播放器
├── _playbackState: StateFlow          # 播放状态
├── _currentPlaylist: StateFlow        # 当前播放列表
├── playMode: PlayMode                # 播放模式
├── shuffleQueue: MutableList<Int>     # 随机播放队列
└── serviceScope: CoroutineScope       # 服务级协程作用域
```

**核心方法：**
```kotlin
fun setPlaylist(songs: List<Song>, startSong: Song? = null, quiet: Boolean = false)
fun playSong(song: Song, quiet: Boolean = false)
fun play()
fun pause()
fun playNext()
fun playPrevious()
fun seekTo(position: Int)
fun togglePlayMode()
fun stop()
```

### 2.4 随机播放实现

- `shuffleQueue`：维护一个随机播放队列
- `insertIntoShuffleQueue(visualIndex)`：当前歌曲切换时，将后续歌曲插入队列
- `peekNextFromShuffle()`：从队列获取下一首
- `peekPreviousFromShuffle()`：从队列获取上一首

### 2.5 音频设备处理

**API 31+：**
```kotlin
audioDeviceCallback = AudioDeviceCallback {
    onDevicesRemoved { devices -> /* 耳机断开 → 暂停 */ }
    onDevicesAdded { devices -> /* 设备重连 → 恢复播放 */ }
}
registerAudioDeviceCallback(audioDeviceCallback)
```

**API < 31：**
```kotlin
noisyReceiver = BroadcastReceiver {
    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> pause()
}
registerReceiver(noisyReceiver, intentFilter)
```

### 2.6 专辑封面提取

```kotlin
private fun extractAlbumArtFromAudio(audioUri: Uri): Bitmap?
// API 29+: openFileDescriptor + MediaMetadataRetriever
// API < 29: MediaStore 查询文件路径
// 缩放到 256x256 避免通知过大
```

### 2.7 通知栏播放控制

- 创建 NotificationChannel (IMPORTANCE_LOW)
- MediaStyle 通知：上一曲 / 播放暂停 / 下一曲
- 显示歌曲标题、艺术家、专辑封面
- PendingIntent 响应播放控制

### 2.8 MediaSession 支持

响应系统媒体按钮：播放/暂停/上一曲/下一曲/SeekTo

## 3. 流程设计

### 3.1 播放一首歌曲流程

```
用户点击歌曲 → playSong(song)
  ├── releaseMediaPlayer() 释放旧播放器
  ├── MediaPlayer.setDataSource(uri)
  ├── MediaPlayer.prepareAsync()
  └── onPrepared → start() → 更新状态 → 加载封面 → 更新通知 → 添加最近播放
```

### 3.2 切歌流程

```
上一曲 / 下一曲
  ├── 随机模式 → 从 shuffleQueue 获取
  └── 非随机 → 索引 +1 / -1 → 边界处理（循环或停止）
  └── playSong(targetSong)
```

### 3.3 播放模式切换流程

```
点击切换播放模式按钮
  └── LIST_LOOP → OFF → SHUFFLE → ONE_LOOP → LIST_LOOP
      └── 更新 _playbackState.playMode
```

## 4. UI 设计

### 4.1 首页歌曲列表

```
┌─────────────────────────────────┐
│  ☰  音乐播放器        [搜索]     │
├─────────────────────────────────┤
│  🎵 Song Title A                │
│     Artist A  •  03:45          │
├─────────────────────────────────┤
│  🎵 Song Title B                │
│     Artist B  •  04:12          │
└─────────────────────────────────┘
│  ▶ 正在播放: Song A              │
│  [上一首] [暂停] [下一首]        │
└─────────────────────────────────┘
```

### 4.2 播放详情页

```
┌─────────────────────────────────┐
│  ▼  Song Title                  │
├─────────────────────────────────┤
│         ┌──────────┐             │
│         │  封面    │             │
│         │  (旋转)  │             │
│         └──────────┘             │
│                                 │
│         Song Title               │
│         Artist Name              │
│                                 │
│  ──────────●─────────────  03:45 │
│                          04:12   │
│                                 │
│  [🔀]  [⏮]  [⏯]  [⏭]  [🎵]    │
└─────────────────────────────────┘
```

### 4.3 MiniPlayer

```
┌─────────────────────────────────┐
│  🎵 Song A    ⏮  ▶  ⏭         │
└─────────────────────────────────┘
```

## 5. 数据模型变更

### 5.1 新增实体

- `SongEntity` - 歌曲数据库实体

### 5.2 Repository 层新增方法

```kotlin
suspend fun scanMusicFromDevice()
fun getAllSongs(): Flow<List<Song>>
fun searchSongs(query: String): Flow<List<Song>>
suspend fun getSongById(id: Long): Song?
suspend fun getSongsByIds(ids: List<Long>): List<Song>
suspend fun getSongUri(songId: Long): Uri?
suspend fun hasSongs(): Boolean
```

### 5.3 Service 层新增方法

```kotlin
// 播放列表
fun setPlaylist(songs: List<Song>, startSong: Song? = null, quiet: Boolean = false, playlistId: String? = null)
fun playSong(song: Song, quiet: Boolean = false)
fun syncPlaylist(playlistId: String, songs: List<Song>)
fun setSortedLocalMusicFlow(flow: Flow<List<Song>>)

// 播放控制
fun play()
fun pause()
fun playNext()
fun playPrevious()
fun seekTo(position: Int)
fun togglePlayMode()
fun stop()

// 状态
val playbackState: StateFlow<PlaybackState>
val currentPlaylist: StateFlow<List<Song>>
```

### 5.4 ViewModel 层新增方法

```kotlin
val sortedLocalMusic: StateFlow<List<Song>>

fun playSong(song: Song)
fun playSongs(songs: List<Song>, startSong: Song, playlistId: String = "local")
fun playSongAtIndex(index: Int)
fun playSongsQuietly(songs: List<Song>, startSong: Song)
fun playPlaylist(playlistId: Long)
fun togglePlayPause()
fun playNext()
fun playPrevious()
fun seekTo(position: Long)
fun toggleShuffle()
fun toggleRepeat()
```

## 6. 修改文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `domain/model/Song.kt` | 新建 | 歌曲领域模型 |
| `domain/model/PlaybackState.kt` | 新建 | 播放状态和播放模式 |
| `data/local/SongEntity.kt` | 新建 | 歌曲数据库实体 |
| `data/local/SongDao.kt` | 新建 | 歌曲 DAO |
| `data/local/MusicDatabase.kt` | 修改 | 注册 SongDao |
| `data/repository/MusicRepository.kt` | 修改 | 添加扫描和查询方法 |
| `presentation/viewmodel/MusicViewModel.kt` | 修改 | 添加播放控制方法 |
| `service/MusicPlaybackService.kt` | 新建 | 播放服务和通知控制 |
| `presentation/ui/MainActivity.kt` | 修改 | 绑定播放服务 |
| `presentation/ui/screens/home/HomeScreen.kt` | 新建 | 首页歌曲列表 |
| `presentation/ui/screens/playdetail/PlayDetailScreen.kt` | 新建 | 播放详情页 |
| `presentation/ui/components/MiniPlayer.kt` | 新建 | 迷你播放器 |
| `presentation/navigation/MusicNavHost.kt` | 修改 | 导航路由 |

## 7. 成功/失败提示

- 扫描完成：静默完成，无需提示
- 播放失败：Toast "无法播放此歌曲"
- 无音乐：空列表提示"暂无音乐，请添加音乐到设备"
- 权限被拒：引导用户授权的提示对话框
