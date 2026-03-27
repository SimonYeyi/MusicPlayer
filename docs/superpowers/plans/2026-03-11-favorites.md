# 收藏功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现歌曲收藏功能，支持收藏/取消收藏，显示收藏列表。

**Architecture:**
- 数据层：`FavoriteEntity`（Room）
- DAO 层：`FavoriteDao`（Flow 查询）
- Repository 层：`MusicRepository`（业务封装）
- ViewModel 层：`MusicViewModel`（UI 绑定）
- UI 层：歌曲列表中的收藏菜单项

**Tech Stack:** Room Database, Kotlin Coroutines + Flow, Jetpack Compose, Hilt

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `data/local/FavoriteEntity.kt` | 收藏数据库实体 |
| `data/local/FavoriteDao.kt` | 收藏数据访问对象 |
| `data/local/MusicDatabase.kt` | 注册 DAO |
| `data/repository/MusicRepository.kt` | 收藏业务逻辑 |
| `presentation/viewmodel/MusicViewModel.kt` | 收藏 UI 状态和方法 |

---

## Task 1: 创建收藏数据实体和 DAO

**Files:**
- Create: `data/local/FavoriteEntity.kt`
- Create: `data/local/FavoriteDao.kt`
- Modify: `data/local/MusicDatabase.kt`
- Modify: `di/AppModule.kt`

- [ ] **Step 1: 创建 FavoriteEntity**

  ```kotlin
  @Entity(
      tableName = "favorites",
      indices = [Index("songId", unique = true)]
  )
  data class FavoriteEntity(
      @PrimaryKey(autoGenerate = true)
      val id: Long = 0,
      val songId: Long,
      val addedAt: Long = System.currentTimeMillis()
  )
  ```

- [ ] **Step 2: 创建 FavoriteDao**

  ```kotlin
  @Dao
  interface FavoriteDao {
      fun getAllFavorites(): Flow<List<FavoriteEntity>>
      fun getAllFavoriteSongIds(): Flow<List<Long>>
      suspend fun addFavorite(favorite: FavoriteEntity): Long
      suspend fun removeFavorite(songId: Long)
      suspend fun isFavorite(songId: Long): Boolean
      fun isFavoriteFlow(songId: Long): Flow<Boolean>
  }
  ```

- [ ] **Step 3: MusicDatabase 注册**

  ```kotlin
  @Database(..., entities = [..., FavoriteEntity::class], ...)
  abstract class MusicDatabase : RoomDatabase() {
      abstract fun favoriteDao(): FavoriteDao
  }
  ```

- [ ] **Step 4: AppModule 提供 DAO**

  ```kotlin
  @Provides
  @Singleton
  fun provideFavoriteDao(database: MusicDatabase): FavoriteDao
  ```

- [ ] **Commit**
  ```bash
  git add data/local/FavoriteEntity.kt data/local/FavoriteDao.kt data/local/MusicDatabase.kt di/AppModule.kt
  git commit -m "feat: 添加收藏实体和 DAO"
  ```

---

## Task 2: Repository 添加收藏方法

**Files:**
- Modify: `data/repository/MusicRepository.kt`

- [ ] **Step 1: 添加字段和方法**

  ```kotlin
  class MusicRepository @Inject constructor(
      private val favoriteDao: FavoriteDao,
      // ...
  ) {
      fun getFavoriteSongIds(): Flow<List<Long>> = favoriteDao.getAllFavoriteSongIds()
      suspend fun addFavorite(songId: Long)
      suspend fun removeFavorite(songId: Long)
      suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)
      fun isFavoriteFlow(songId: Long): Flow<Boolean> = favoriteDao.isFavoriteFlow(songId)
      suspend fun toggleFavorite(songId: Long) {
          if (isFavorite(songId)) removeFavorite(songId)
          else addFavorite(songId)
      }
  }
  ```

- [ ] **Commit**
  ```bash
  git add data/repository/MusicRepository.kt
  git commit -m "feat: MusicRepository 添加收藏方法"
  ```

---

## Task 3: ViewModel 添加收藏状态和方法

**Files:**
- Modify: `presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 添加收藏状态到 MusicUiState**

  ```kotlin
  data class MusicUiState(
      val favoriteSongIds: Set<Long> = emptySet(),
      // ...
  )
  ```

- [ ] **Step 2: 在初始化时收集收藏数据**

  ```kotlin
  viewModelScope.launch {
      repository.getFavoriteSongIds().collect { ids ->
          _uiState.update { it.copy(favoriteSongIds = ids.toSet()) }
      }
  }
  ```

- [ ] **Step 3: 添加收藏切换方法**

  ```kotlin
  fun toggleFavorite(songId: Long) {
      viewModelScope.launch {
          repository.toggleFavorite(songId)
      }
  }
  fun isFavorite(songId: Long): Boolean = uiState.value.favoriteSongIds.contains(songId)
  ```

- [ ] **Commit**
  ```bash
  git add presentation/viewmodel/MusicViewModel.kt
  git commit -m "feat: MusicViewModel 添加收藏功能"
  ```

---

## Task 4: 构建与测试

**Files:**
- None

- [ ] **Step 1: 构建验证**

  ```bash
  ./gradlew assembleDebug
  ```

- [ ] **Step 2: 功能测试**

  - [ ] 歌曲列表长按菜单中收藏选项正常
  - [ ] 点击收藏后状态切换
  - [ ] 收藏列表显示正确
  - [ ] 取消收藏后列表同步更新

- [ ] **Commit**
  ```bash
  git add .
  git commit -m "feat: 完成收藏功能"
  ```
