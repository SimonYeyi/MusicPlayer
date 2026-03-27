# 音乐播放核心功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现完整的音乐播放核心功能，包括音乐扫描、播放控制、后台服务、通知栏、随机播放和音频设备处理。

**Architecture:**
- 数据层：`SongEntity` + `SongDao`（Room）
- Repository 层：`MusicRepository`（扫描和查询）
- Service 层：`MusicPlaybackService`（Foreground Service + MediaPlayer）
- ViewModel 层：`MusicViewModel`（UI 绑定和状态）
- UI 层：HomeScreen + PlayDetailScreen + MiniPlayer

**Tech Stack:** Room Database, MediaPlayer, Foreground Service, MediaSession, MediaStore API, Kotlin Coroutines + Flow, Jetpack Compose, Hilt

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `domain/model/Song.kt` | 歌曲领域模型 |
| `domain/model/PlaybackState.kt` | 播放状态和播放模式枚举 |
| `data/local/SongEntity.kt` | 歌曲数据库实体 |
| `data/local/SongDao.kt` | 歌曲数据访问对象 |
| `data/local/MusicDatabase.kt` | 数据库配置 |
| `data/repository/MusicRepository.kt` | 音乐扫描和数据管理 |
| `presentation/viewmodel/MusicViewModel.kt` | 播放状态和 UI 绑定 |
| `service/MusicPlaybackService.kt` | 播放服务和通知控制 |
| `presentation/ui/MainActivity.kt` | 服务绑定 |
| `presentation/ui/screens/home/HomeScreen.kt` | 首页歌曲列表 |
| `presentation/ui/screens/playdetail/PlayDetailScreen.kt` | 播放详情页 |
| `presentation/ui/components/MiniPlayer.kt` | 迷你播放器 |
| `presentation/navigation/MusicNavHost.kt` | 导航路由 |

---

## Task 1: 创建歌曲数据模型

**Files:**
- Create: `domain/model/Song.kt`
- Create: `domain/model/PlaybackState.kt`

- [ ] **Step 1: 创建 Song.kt**

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

- [ ] **Step 2: 创建 PlaybackState.kt**

  ```kotlin
  data class PlaybackState(
      val currentSong: Song? = null,
      val isPlaying: Boolean = false,
      val currentPosition: Long = 0L,
      val duration: Long = 0L,
      val playMode: PlayMode = PlayMode.LIST_LOOP
  )

  enum class PlayMode {
      OFF,
      LIST_LOOP,
      SHUFFLE,
      ONE_LOOP
  }
  ```

- [ ] **Commit**
  ```bash
  git add domain/model/Song.kt domain/model/PlaybackState.kt
  git commit -m "feat: 添加歌曲模型和播放状态"
  ```

---

## Task 2: 创建歌曲数据层

**Files:**
- Create: `data/local/SongEntity.kt`
- Create: `data/local/SongDao.kt`
- Modify: `data/local/MusicDatabase.kt`
- Modify: `di/AppModule.kt`

- [ ] **Step 1: 创建 SongEntity**

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

- [ ] **Step 2: 创建 SongDao**

  ```kotlin
  @Dao
  interface SongDao {
      fun getAllSongs(): Flow<List<SongEntity>>
      fun searchSongs(query: String): Flow<List<SongEntity>>
      suspend fun getSongById(id: Long): SongEntity?
      suspend fun getSongsByIds(ids: List<Long>): List<SongEntity>
      suspend fun insertSong(song: SongEntity)
      suspend fun insertSongs(songs: List<SongEntity>)
      suspend fun deleteSong(id: Long)
      suspend fun clearAll()
      suspend fun hasSongs(): Boolean
  }
  ```

- [ ] **Step 3: MusicDatabase 注册**

  ```kotlin
  @Database(entities = [SongEntity::class, ...], version = 1)
  abstract class MusicDatabase : RoomDatabase() {
      abstract fun songDao(): SongDao
  }
  ```

- [ ] **Step 4: AppModule 提供 SongDao**

  ```kotlin
  @Provides @Singleton fun provideSongDao(database: MusicDatabase): SongDao
  ```

- [ ] **Commit**
  ```bash
  git add data/local/SongEntity.kt data/local/SongDao.kt data/local/MusicDatabase.kt di/AppModule.kt
  git commit -m "feat: 添加歌曲数据层"
  ```

---

## Task 3: Repository 添加扫描和查询方法

**Files:**
- Create: `data/repository/MusicRepository.kt`

- [ ] **Step 1: 创建 MusicRepository**

  核心方法：`scanMusicFromDevice()` 使用 MediaStore API 扫描

  ```kotlin
  class MusicRepository @Inject constructor(
      private val songDao: SongDao,
      private val contentResolver: ContentResolver
  ) {
      suspend fun scanMusicFromDevice() { /* MediaStore 查询 */ }
      fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
      fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
      suspend fun getSongById(id: Long): Song?
      suspend fun getSongsByIds(ids: List<Long>): List<Song>
      suspend fun hasSongs(): Boolean
  }
  ```

- [ ] **Commit**
  ```bash
  git add data/repository/MusicRepository.kt
  git commit -m "feat: MusicRepository 添加音乐扫描和查询"
  ```

---

## Task 4: 创建播放服务

**Files:**
- Create: `service/MusicPlaybackService.kt`

- [ ] **Step 1: 创建 Foreground Service**

  核心组件：
  - `MediaPlayer` - 音频播放
  - `ServiceNotification` - 通知栏
  - `MediaSession` - 系统媒体按钮
  - `AudioDeviceCallback` - 音频设备切换
  - `Handler` - 进度更新（每 1 秒更新一次 position）

- [ ] **Step 2: 核心方法实现**

  ```kotlin
  class MusicPlaybackService : Service() {
      private var mediaPlayer: MediaPlayer? = null
      private val _playbackState = MutableStateFlow(PlaybackState())
      private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
      private var playMode = PlayMode.LIST_LOOP
      private var shuffleQueue = mutableListOf<Int>()

      fun setPlaylist(songs: List<Song>, startSong: Song?, quiet: Boolean)
      fun playSong(song: Song, quiet: Boolean = false)
      fun play() { mediaPlayer?.start() }
      fun pause() { mediaPlayer?.pause() }
      fun playNext()
      fun playPrevious()
      fun seekTo(position: Int)
      fun togglePlayMode()
      fun stop()
  }
  ```

- [ ] **Step 3: 通知栏实现**

  ```kotlin
  // NotificationChannel + MediaStyle 通知
  // PendingIntent 响应播放控制
  // MediaStyle 样式：上一曲 / 播放暂停 / 下一曲
  ```

- [ ] **Step 4: MediaSession 实现**

  ```kotlin
  // 响应系统媒体按钮
  // onPlay / onPause / onSkipToNext / onSkipToPrevious / onSeekTo
  ```

- [ ] **Step 5: 音频设备切换**

  ```kotlin
  // API 31+: AudioDeviceCallback
  // API < 31: BroadcastReceiver (ACTION_AUDIO_BECOMING_NOISY)
  ```

- [ ] **Commit**
  ```bash
  git add service/MusicPlaybackService.kt
  git commit -m "feat: 添加音乐播放服务"
  ```

---

## Task 5: ViewModel 添加播放控制

**Files:**
- Modify: `presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 添加播放相关状态和方法**

  ```kotlin
  data class MusicUiState(
      val songs: List<Song> = emptyList(),
      val playbackState: PlaybackState = PlaybackState(),
      val currentPlaylist: List<Song> = emptyList(),
      // ...
  )

  // 方法
  fun playSong(song: Song)
  fun playSongs(songs: List<Song>, startSong: Song, playlistId: String = "local")
  fun playSongAtIndex(index: Int)
  fun togglePlayPause()
  fun playNext()
  fun playPrevious()
  fun seekTo(position: Long)
  fun toggleShuffle()
  fun toggleRepeat()
  ```

- [ ] **Commit**
  ```bash
  git add presentation/viewmodel/MusicViewModel.kt
  git commit -m "feat: MusicViewModel 添加播放控制"
  ```

---

## Task 6: 创建首页和播放详情页

**Files:**
- Create: `presentation/ui/screens/home/HomeScreen.kt`
- Create: `presentation/ui/screens/playdetail/PlayDetailScreen.kt`
- Create: `presentation/ui/components/MiniPlayer.kt`
- Modify: `presentation/navigation/MusicNavHost.kt`
- Modify: `presentation/ui/MainActivity.kt`

- [ ] **Step 1: 创建 HomeScreen**

  歌曲列表 + MiniPlayer + 底部导航

- [ ] **Step 2: 创建 PlayDetailScreen**

  封面旋转动画 + 进度条 + 播放控制 + 播放队列

- [ ] **Step 3: 创建 MiniPlayer**

  当前歌曲信息 + 播放控制

- [ ] **Step 4: 导航配置**

  ```kotlin
  composable("home") { HomeScreen(...) }
  composable("play_detail") { PlayDetailScreen(...) }
  ```

- [ ] **Step 5: MainActivity 绑定 Service**

  ```kotlin
  // bindService 绑定 MusicPlaybackService
  // 通过 Binder 获取 Service 实例
  // 将 Service 的 playbackState 转发到 ViewModel
  ```

- [ ] **Commit**
  ```bash
  git add presentation/ui/screens/home/HomeScreen.kt presentation/ui/screens/playdetail/PlayDetailScreen.kt presentation/ui/components/MiniPlayer.kt presentation/navigation/MusicNavHost.kt presentation/ui/MainActivity.kt
  git commit -m "feat: 添加首页、播放详情页和 MiniPlayer"
  ```

---

## Task 7: 构建与测试

**Files:**
- None

- [ ] **Step 1: 构建验证**

  ```bash
  ./gradlew assembleDebug
  ```

- [ ] **Step 2: 功能测试**

  - [ ] 音乐扫描正常（首次启动扫描）
  - [ ] 点击歌曲播放
  - [ ] 播放/暂停/上一曲/下一曲
  - [ ] 进度条拖动
  - [ ] 播放模式切换（列表循环/单曲循环/随机/关闭）
  - [ ] 后台播放（按 Home 键继续播放）
  - [ ] 通知栏显示和控制
  - [ ] 耳机断开时暂停
  - [ ] 专辑封面显示

- [ ] **Commit**
  ```bash
  git add .
  git commit -m "feat: 完成音乐播放核心功能"
  ```
