# 最近播放功能设计

## 1. 概述

自动记录用户最近播放的歌曲，支持查看最近播放历史和清空历史记录。

## 2. 核心逻辑

### 2.1 数据模型

使用 Room 数据库持久化最近播放记录：

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

- `songId` 上建立唯一索引，每次播放更新 `playedAt` 时间戳
- 最多保留 50 条记录，超出自动清理

### 2.2 DAO 层接口

```kotlin
@Dao
interface RecentPlayDao {
    fun getRecentPlays(limit: Int = 50): Flow<List<RecentPlayEntity>>
    fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>>
    suspend fun addRecentPlay(recentPlay: RecentPlayEntity)
    suspend fun removeFromRecentPlays(songId: Long)
    suspend fun cleanupOldPlays(keepCount: Int = 100)
    suspend fun clearAll()
}
```

### 2.3 业务规则

- 每次正常播放（非静默模式）自动添加到最近播放
- 使用 `INSERT OR REPLACE` 语义，同一歌曲重复播放只更新时间戳
- 最多保留 50 条，超出时自动清理最旧的记录
- 静默模式（quiet=true）不更新最近播放（用于最近播放列表自身播放）

## 3. 流程设计

### 3.1 自动添加最近播放流程

```
用户播放歌曲
  ├── quiet = false（正常播放）
  │    └── 添加到 recent_plays（相同 songId 则更新 playedAt）
  └── quiet = true（静默播放）
       └── 不更新最近播放
```

### 3.2 清理旧记录流程

```
添加新记录后 → 检查总数是否超过 50
  ├── 超过 → 删除最旧的记录
  └── 未超过 → 不处理
```

### 3.3 清空历史流程

```
用户点击"清空历史" → 显示确认对话框 → 确认清空
  └── DELETE FROM recent_plays
```

## 4. UI 设计

### 4.1 最近播放入口（我的页面）

```
┌─────────────────────────────────┐
│  🕐 最近播放                    │
│     X 首歌曲                    │
└─────────────────────────────────┘
```

### 4.2 最近播放列表

与普通歌曲列表类似，点击播放，长按显示菜单（支持从历史移除）。

## 5. 数据模型变更

### 5.1 新增表

- `recent_plays` - 最近播放表

### 5.2 Repository 层变更

```kotlin
fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>>
suspend fun addRecentPlay(songId: Long)
suspend fun removeFromRecentPlays(songId: Long)
suspend fun clearRecentPlays()
```

### 5.3 ViewModel 层变更

```kotlin
val recentSongIds: List<Long>

fun getRecentSongs(): List<Song>
fun clearRecentPlays()
fun removeFromRecentPlays(songId: Long)
```

## 6. 修改文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `data/local/RecentPlayEntity.kt` | 新建 | 最近播放实体 |
| `data/local/RecentPlayDao.kt` | 新建 | 最近播放 DAO |
| `data/local/MusicDatabase.kt` | 修改 | 注册 DAO 和实体 |
| `data/repository/MusicRepository.kt` | 修改 | 添加最近播放方法 |
| `presentation/viewmodel/MusicViewModel.kt` | 修改 | 添加最近播放状态和方法 |
| `service/MusicPlaybackService.kt` | 修改 | 播放时自动添加最近播放 |

## 7. 成功/失败提示

- 清空历史：确认对话框
- 从历史移除：Toast "已从历史移除"
