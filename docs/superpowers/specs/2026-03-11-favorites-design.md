# 收藏功能设计

## 1. 概述

提供歌曲收藏功能，用户可以将喜欢的歌曲收藏到收藏夹，支持快速切换收藏状态。

## 2. 核心逻辑

### 2.1 数据模型

使用 Room 数据库持久化收藏记录：

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

- `songId` 上建立唯一索引，保证每首歌只能被收藏一次

### 2.2 DAO 层接口

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

### 2.3 业务规则

- 收藏状态切换使用 `toggleFavorite`：已收藏则取消，未收藏则添加
- 通过 SharedFlow 通知收藏状态变化，UI 实时响应
- 收藏夹中歌曲按添加时间倒序排列

## 3. 流程设计

### 3.1 切换收藏状态流程

```
用户点击收藏按钮
  ├── 当前未收藏 → 添加到 favorites → 更新 UI → Toast "已收藏"
  └── 当前已收藏 → 从 favorites 移除 → 更新 UI → Toast "已取消收藏"
```

### 3.2 查看收藏列表流程

```
进入"我的"页面 → 显示收藏入口 → 点击进入收藏页
  └── 查询 favorites 表 → 关联 songs 表 → 显示收藏歌曲列表
```

## 4. UI 设计

### 4.1 收藏入口（我的页面）

```
┌─────────────────────────────────┐
│  ❤️ 收藏                       │
│     X 首歌曲                    │
└─────────────────────────────────┘
```

### 4.2 收藏歌曲列表

与普通歌曲列表相同，点击播放，长按显示菜单。

## 5. 数据模型变更

### 5.1 新增表

- `favorites` - 收藏表

### 5.2 Repository 层变更

```kotlin
fun getFavoriteSongIds(): Flow<List<Long>>
suspend fun addFavorite(songId: Long)
suspend fun removeFavorite(songId: Long)
suspend fun isFavorite(songId: Long): Boolean
fun isFavoriteFlow(songId: Long): Flow<Boolean>
suspend fun toggleFavorite(songId: Long)
```

### 5.3 ViewModel 层变更

```kotlin
// 在 MusicUiState 中
val favoriteSongIds: Set<Long>

// 方法
fun toggleFavorite(songId: Long)
fun isFavorite(songId: Long): Boolean
```

## 6. 修改文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `data/local/FavoriteEntity.kt` | 新建 | 收藏实体 |
| `data/local/FavoriteDao.kt` | 新建 | 收藏 DAO |
| `data/local/MusicDatabase.kt` | 修改 | 注册 DAO 和实体 |
| `data/repository/MusicRepository.kt` | 修改 | 添加收藏方法 |
| `presentation/viewmodel/MusicViewModel.kt` | 修改 | 添加收藏状态和方法 |

## 7. 成功/失败提示

- 收藏成功：Toast "已收藏"
- 取消收藏：Toast "已取消收藏"
