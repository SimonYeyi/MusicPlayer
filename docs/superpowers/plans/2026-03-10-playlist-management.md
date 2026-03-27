# 歌单管理功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现完整的播放列表管理功能，包括创建、重命名、删除歌单，添加、移除、移动歌曲。

**Architecture:**
- 数据层：`PlaylistEntity` + `PlaylistSongEntity`（Room）
- DAO 层：`PlaylistDao`（Flow 查询）
- Repository 层：`MusicRepository`（业务封装）
- ViewModel 层：`MusicViewModel`（UI 绑定）
- UI 层：`PlaylistScreen` + `PlaylistPickerDialog`

**Tech Stack:** Room Database, Kotlin Coroutines + Flow, Jetpack Compose, Hilt

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `data/local/PlaylistEntity.kt` | 歌单数据库实体 |
| `data/local/PlaylistSongEntity.kt` | 歌单歌曲关联实体 |
| `data/local/PlaylistDao.kt` | 歌单数据访问对象 |
| `data/repository/MusicRepository.kt` | 歌单业务逻辑封装 |
| `presentation/viewmodel/MusicViewModel.kt` | 歌单 UI 状态和方法 |
| `presentation/ui/screens/playlist/PlaylistScreen.kt` | 歌单列表页面 |
| `presentation/ui/components/PlaylistPickerDialog.kt` | 歌单选择对话框 |
| `presentation/navigation/MusicNavHost.kt` | 歌单路由配置 |

---

## Task 1: 创建歌单数据实体

**Files:**
- Create: `data/local/PlaylistEntity.kt`
- Create: `data/local/PlaylistSongEntity.kt`

- [ ] **Step 1: 创建 PlaylistEntity**

  ```kotlin
  @Entity(tableName = "playlists")
  data class PlaylistEntity(
      @PrimaryKey(autoGenerate = true)
      val id: Long = 0,
      val name: String,
      val createdAt: Long = System.currentTimeMillis(),
      val songCount: Int = 0
  )
  ```

- [ ] **Step 2: 创建 PlaylistSongEntity（关联表）**

  ```kotlin
  @Entity(
      tableName = "playlist_songs",
      primaryKeys = ["playlistId", "songId"],
      foreignKeys = [
          ForeignKey(
              entity = PlaylistEntity::class,
              parentColumns = ["id"],
              childColumns = ["playlistId"],
              onDelete = ForeignKey.CASCADE
          )
      ],
      indices = [Index("playlistId"), Index("songId")]
  )
  data class PlaylistSongEntity(
      val playlistId: Long,
      val songId: Long,
      val addedAt: Long = System.currentTimeMillis()
  )
  ```

- [ ] **Commit**
  ```bash
  git add data/local/PlaylistEntity.kt data/local/PlaylistSongEntity.kt
  git commit -m "feat: 添加歌单和关联表实体"
  ```

---

## Task 2: 创建歌单 DAO

**Files:**
- Create: `data/local/PlaylistDao.kt`

- [ ] **Step 1: 创建 PlaylistDao 接口**

  ```kotlin
  @Dao
  interface PlaylistDao {
      fun getAllPlaylists(): Flow<List<PlaylistEntity>>
      suspend fun getPlaylistById(id: Long): PlaylistEntity?
      suspend fun insertPlaylist(playlist: PlaylistEntity): Long
      suspend fun updatePlaylistName(playlistId: Long, name: String)
      suspend fun deletePlaylist(playlist: PlaylistEntity)
      suspend fun deletePlaylistById(id: Long)
      fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>
      suspend fun getSongIdsInPlaylistDirect(playlistId: Long): List<Long>
      suspend fun addSongToPlaylist(playlistSong: PlaylistSongEntity)
      suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
      suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
      suspend fun getSongCount(playlistId: Long): Int
      suspend fun updateSongCount(playlistId: Long)
  }
  ```

- [ ] **Step 2: 在 MusicDatabase 中注册 DAO**

  ```kotlin
  @Database(..., entities = [..., PlaylistEntity::class, PlaylistSongEntity::class], ...)
  abstract class MusicDatabase : RoomDatabase() {
      abstract fun playlistDao(): PlaylistDao
  }
  ```

- [ ] **Step 3: 在 AppModule 中提供 DAO**

  ```kotlin
  @Provides
  @Singleton
  fun providePlaylistDao(database: MusicDatabase): PlaylistDao
  ```

- [ ] **Commit**
  ```bash
  git add data/local/PlaylistDao.kt data/local/MusicDatabase.kt di/AppModule.kt
  git commit -m "feat: 添加 PlaylistDao 和数据库注册"
  ```

---

## Task 3: Repository 添加歌单 CRUD 方法

**Files:**
- Modify: `data/repository/MusicRepository.kt`

- [ ] **Step 1: 添加字段和方法**

  ```kotlin
  class MusicRepository @Inject constructor(
      private val songDao: SongDao,
      private val playlistDao: PlaylistDao,
      // ...
  ) {
      // 歌单
      fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
      suspend fun createPlaylist(name: String): Long
      suspend fun deletePlaylist(playlistId: Long)
      suspend fun renamePlaylist(playlistId: Long, newName: String)
      fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>
      suspend fun getSongsForPlaylist(playlistId: Long): List<Song>
      suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
      suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
      suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
  }
  ```

- [ ] **Commit**
  ```bash
  git add data/repository/MusicRepository.kt
  git commit -m "feat: MusicRepository 添加歌单 CRUD 方法"
  ```

---

## Task 4: ViewModel 添加歌单管理方法

**Files:**
- Modify: `presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 在 MusicUiState 中添加歌单相关状态**

  ```kotlin
  data class MusicUiState(
      // ...
      val playlists: List<PlaylistEntity> = emptyList(),
      // ...
  )
  ```

- [ ] **Step 2: 添加歌单管理方法**

  ```kotlin
  suspend fun createPlaylist(name: String): Long
  fun deletePlaylist(playlistId: Long)
  fun renamePlaylist(playlistId: Long, newName: String)
  fun addToPlaylist(playlistId: Long, songId: Long)
  fun moveSongToPlaylist(fromPlaylistId: Long, toPlaylistId: Long, songId: Long)
  fun moveSongsToPlaylist(fromPlaylistId: Long, toPlaylistId: Long, songIds: List<Long>)
  fun removeFromPlaylist(playlistId: Long, songId: Long)
  fun clearPlaylist(playlistId: Long)
  suspend fun getPlaylistSongs(playlistId: Long): List<Song>
  ```

- [ ] **Commit**
  ```bash
  git add presentation/viewmodel/MusicViewModel.kt
  git commit -m "feat: MusicViewModel 添加歌单管理方法"
  ```

---

## Task 5: 创建歌单选择对话框

**Files:**
- Create: `presentation/ui/components/PlaylistPickerDialog.kt`

- [ ] **Step 1: 创建 PlaylistPickerDialog**

  支持：选择已有歌单、新建歌单、排除特定歌单（移动场景）

  ```kotlin
  @Composable
  fun PlaylistPickerDialog(
      playlists: List<PlaylistEntity>,
      onDismiss: () -> Unit,
      onPlaylistSelected: (Long) -> Unit,
      onPlaylistCreated: (String) -> Long,
      excludePlaylistId: Long? = null
  )
  ```

- [ ] **Commit**
  ```bash
  git add presentation/ui/components/PlaylistPickerDialog.kt
  git commit -m "feat: 添加歌单选择对话框"
  ```

---

## Task 6: 创建歌单页面

**Files:**
- Create: `presentation/ui/screens/playlist/PlaylistScreen.kt`
- Modify: `presentation/navigation/MusicNavHost.kt`

- [ ] **Step 1: 创建 PlaylistScreen**

  功能：歌单列表展示、创建歌单、删除歌单、歌单详情页（歌曲列表）、编辑模式（批量操作）

  ```kotlin
  @Composable
  fun PlaylistScreen(
      playlistId: Long,
      onBackClick: () -> Unit
  )
  ```

- [ ] **Step 2: 添加路由**

  ```kotlin
  composable("playlist/{playlistId}") { backStackEntry ->
      PlaylistScreen(playlistId = backStackEntry.arguments?.getString("playlistId")?.toLong() ?: return@composable)
  }
  ```

- [ ] **Commit**
  ```bash
  git add presentation/ui/screens/playlist/PlaylistScreen.kt presentation/navigation/MusicNavHost.kt
  git commit -m "feat: 添加歌单页面和路由"
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

  - [ ] 创建歌单
  - [ ] 重命名歌单
  - [ ] 删除歌单（确认关联数据一并删除）
  - [ ] 添加歌曲到歌单
  - [ ] 从歌单移除歌曲
  - [ ] 批量移动歌曲
  - [ ] 歌单选择对话框（添加到歌单）
  - [ ] 歌单选择对话框（移动到歌单）

- [ ] **Commit**
  ```bash
  git add .
  git commit -m "feat: 完成歌单管理功能"
  ```
