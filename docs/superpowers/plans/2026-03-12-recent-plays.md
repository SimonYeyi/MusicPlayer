# 最近播放功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现最近播放记录功能，自动记录播放历史，支持查看和清空历史。

**Architecture:**
- 数据层：`RecentPlayEntity`（Room）
- DAO 层：`RecentPlayDao`（Flow 查询）
- Repository 层：`MusicRepository`（业务封装）
- ViewModel 层：`MusicViewModel`（UI 绑定）
- Service 层：`MusicPlaybackService`（自动记录）

**Tech Stack:** Room Database, Kotlin Coroutines + Flow, Jetpack Compose, Hilt

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `data/local/RecentPlayEntity.kt` | 最近播放数据库实体 |
| `data/local/RecentPlayDao.kt` | 最近播放数据访问对象 |
| `data/local/MusicDatabase.kt` | 注册 DAO |
| `data/repository/MusicRepository.kt` | 最近播放业务逻辑 |
| `presentation/viewmodel/MusicViewModel.kt` | 最近播放 UI 状态和方法 |
| `service/MusicPlaybackService.kt` | 播放时自动添加记录 |

---

## Task 1: 创建最近播放数据实体和 DAO

**Files:**
- Create: `data/local/RecentPlayEntity.kt`
- Create: `data/local/RecentPlayDao.kt`
- Modify: `data/local/MusicDatabase.kt`
- Modify: `di/AppModule.kt`

- [ ] **Step 1: 创建 RecentPlayEntity**

  ```kotlin
  @Entity(
      tableName = "recent_plays",
      indices = [Index("songId", unique = true)]
  )
  data class RecentPlayEntity(
      @PrimaryKey(autoGenerate = true)
      val id: Long = 0,
      val songId: Long,
      val playedAt: Long = System.currentTimeMillis()
  )
  ```

- [ ] **Step 2: 创建 RecentPlayDao**

  ```kotlin
  @Dao
  interface RecentPlayDao {
      fun getRecentPlays(limit: Int = 50): Flow<List<RecentPlayEntity>>
      fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>>
      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun addRecentPlay(recentPlay: RecentPlayEntity)
      @Query("DELETE FROM recent_plays WHERE songId = :songId")
      suspend fun removeFromRecentPlays(songId: Long)
      @Query("DELETE FROM recent_plays WHERE id NOT IN (SELECT id FROM recent_plays ORDER BY playedAt DESC LIMIT :keepCount)")
      suspend fun cleanupOldPlays(keepCount: Int = 100)
      @Query("DELETE FROM recent_plays")
      suspend fun clearAll()
  }
  ```

- [ ] **Step 3: MusicDatabase 注册**

  ```kotlin
  @Database(..., entities = [..., RecentPlayEntity::class], ...)
  abstract class MusicDatabase : RoomDatabase() {
      abstract fun recentPlayDao(): RecentPlayDao
  }
  ```

- [ ] **Step 4: AppModule 提供 DAO**

  ```kotlin
  @Provides
  @Singleton
  fun provideRecentPlayDao(database: MusicDatabase): RecentPlayDao
  ```

- [ ] **Commit**
  ```bash
  git add data/local/RecentPlayEntity.kt data/local/RecentPlayDao.kt data/local/MusicDatabase.kt di/AppModule.kt
  git commit -m "feat: 添加最近播放实体和 DAO"
  ```

---

## Task 2: Repository 添加最近播放方法

**Files:**
- Modify: `data/repository/MusicRepository.kt`

- [ ] **Step 1: 添加字段和方法**

  ```kotlin
  class MusicRepository @Inject constructor(
      private val recentPlayDao: RecentPlayDao,
      // ...
  ) {
      fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>> =
          recentPlayDao.getRecentPlaySongIds(limit)

      suspend fun addRecentPlay(songId: Long) {
          recentPlayDao.addRecentPlay(RecentPlayEntity(songId = songId))
          recentPlayDao.cleanupOldPlays()
      }

      suspend fun removeFromRecentPlays(songId: Long) {
          recentPlayDao.removeFromRecentPlays(songId)
      }

      suspend fun clearRecentPlays() {
          recentPlayDao.clearAll()
      }
  }
  ```

- [ ] **Commit**
  ```bash
  git add data/repository/MusicRepository.kt
  git commit -m "feat: MusicRepository 添加最近播放方法"
  ```

---

## Task 3: ViewModel 添加最近播放状态和方法

**Files:**
- Modify: `presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 添加最近播放状态到 MusicUiState**

  ```kotlin
  data class MusicUiState(
      val recentSongIds: List<Long> = emptyList(),
      // ...
  )
  ```

- [ ] **Step 2: 在初始化时收集最近播放数据**

  ```kotlin
  viewModelScope.launch {
      repository.getRecentPlaySongIds().collect { ids ->
          _uiState.update { it.copy(recentSongIds = ids) }
      }
  }
  ```

- [ ] **Step 3: 添加最近播放方法**

  ```kotlin
  fun getRecentSongs(): List<Song> {
      val state = uiState.value
      return state.recentSongIds.mapNotNull { songId ->
          state.songs.find { it.id == songId }
      }
  }

  fun clearRecentPlays() {
      viewModelScope.launch {
          repository.clearRecentPlays()
      }
  }

  fun removeFromRecentPlays(songId: Long) {
      viewModelScope.launch {
          repository.removeFromRecentPlays(songId)
      }
  }
  ```

- [ ] **Commit**
  ```bash
  git add presentation/viewmodel/MusicViewModel.kt
  git commit -m "feat: MusicViewModel 添加最近播放功能"
  ```

---

## Task 4: Service 层自动添加最近播放

**Files:**
- Modify: `service/MusicPlaybackService.kt`

- [ ] **Step 1: 在 prepareAndPlay 的 setOnPreparedListener 中添加**

  ```kotlin
  setOnPreparedListener { mp ->
      // ...
      if (!quiet) {
          serviceScope.launch {
              musicRepository.addRecentPlay(song.id)
          }
      }
      // ...
  }
  ```

- [ ] **Commit**
  ```bash
  git add service/MusicPlaybackService.kt
  git commit -m "feat: 播放时自动添加最近播放记录"
  ```

---

## Task 5: 构建与测试

**Files:**
- None

- [ ] **Step 1: 构建验证**

  ```bash
  ./gradlew assembleDebug
  ```

- [ ] **Step 2: 功能测试**

  - [ ] 正常播放歌曲后查看最近播放列表
  - [ ] 同一歌曲重复播放，时间戳更新
  - [ ] 超过 50 首后自动清理旧记录
  - [ ] 清空历史后列表为空
  - [ ] 从历史移除单条记录

- [ ] **Commit**
  ```bash
  git add .
  git commit -m "feat: 完成最近播放功能"
  ```
