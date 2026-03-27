# 歌单管理功能设计

## 1. 概述

提供完整的播放列表管理能力，支持创建、重命名、删除歌单，以及添加、移除、移动歌曲。

## 2. 核心逻辑

### 2.1 数据模型

歌单使用 Room 数据库持久化，包含两张关联表：

**PlaylistEntity** - 歌单本身
```kotlin
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val songCount: Int = 0
)
```

**PlaylistSongEntity** - 歌单与歌曲的关联（多对多）
```kotlin
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE  // 删除歌单时级联删除关联
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

### 2.2 DAO 层接口

```kotlin
@Dao
interface PlaylistDao {
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun getPlaylistById(id: Long): PlaylistEntity?
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    suspend fun updatePlaylistName(playlistId: Long, name: String)
    suspend fun deletePlaylistById(id: Long)
    fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>
    suspend fun addSongToPlaylist(playlistSong: PlaylistSongEntity)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
    suspend fun getSongCount(playlistId: Long): Int
    suspend fun updateSongCount(playlistId: Long)
}
```

### 2.3 业务规则

- 删除歌单时，`ForeignKey.CASCADE` 自动删除所有关联记录
- 添加歌曲后自动更新 `songCount`
- 支持将歌曲从一个歌单移动到另一个歌单
- 支持批量移动多首歌曲

## 3. 流程设计

### 3.1 创建歌单流程

```
用户点击"+" → 输入歌单名 → 确认
  ├── 名称为空 → Toast 提示"请输入歌单名称"
  └── 名称有效 → 创建歌单 → 刷新列表
```

### 3.2 删除歌单流程

```
用户长按歌单 → 显示删除选项 → 确认删除
  └── 级联删除所有 playlist_songs 关联记录
```

### 3.3 添加歌曲到歌单流程

```
用户长按歌曲 → 选择"添加到歌单" → 显示歌单选择对话框
  ├── 选择已有歌单 → 添加关联记录 → 更新 songCount
  └── 新建歌单 → 创建歌单 → 添加关联记录
```

### 3.4 批量移动歌曲流程

```
编辑模式 → 多选歌曲 → 点击"移动" → 选择目标歌单
  └── 逐首从源歌单移除，添加到目标歌单
```

## 4. UI 设计

### 4.1 歌单列表（PlaylistScreen）

```
┌─────────────────────────────────┐
│ ← 我的音乐          [编辑] [+]  │
├─────────────────────────────────┤
│  🎵 我的收藏                    │
│     5 首歌曲                    │
├─────────────────────────────────┤
│  🎵 健身歌单                    │
│     12 首歌曲                   │
├─────────────────────────────────┤
│  🎵 睡前音乐                    │
│     8 首歌曲                    │
└─────────────────────────────────┘
```

### 4.2 歌单详情页（编辑模式）

```
┌─────────────────────────────────┐
│ ← 健身歌单           [完成]     │
├─────────────────────────────────┤
│  ☑ 全选          已选 3/12     │
├─────────────────────────────────┤
│  ☑ Song A        Artist A      │
│  ☑ Song B        Artist B      │
│  ☐ Song C        Artist C      │
├─────────────────────────────────┤
│  [移动到...]  [从歌单移除]       │
└─────────────────────────────────┘
```

### 4.3 歌单选择对话框（PlaylistPickerDialog）

```
┌─────────────────────────────────┐
│  添加到歌单                      │
├─────────────────────────────────┤
│  ○ 我的收藏                     │
│  ○ 健身歌单                     │
│  ○ 睡前音乐                     │
├─────────────────────────────────┤
│  + 创建新歌单                   │
└─────────────────────────────────┘
```

## 5. 数据模型变更

### 5.1 新增表

- `playlists` - 歌单表
- `playlist_songs` - 歌单歌曲关联表

### 5.2 Repository 层变更

```kotlin
// 新增方法
fun getAllPlaylists(): Flow<List<PlaylistEntity>>
suspend fun createPlaylist(name: String): Long
suspend fun deletePlaylist(playlistId: Long)
suspend fun renamePlaylist(playlistId: Long, newName: String)
fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>>
suspend fun getSongsForPlaylist(playlistId: Long): List<Song>
suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean
fun getPlaylistSongsChanged(): SharedFlow<Pair<Long, List<Song>>>

// ViewModel 层变更
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

## 6. 修改文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `data/local/PlaylistEntity.kt` | 新建 | 歌单实体 |
| `data/local/PlaylistSongEntity.kt` | 新建 | 歌单歌曲关联实体 |
| `data/local/PlaylistDao.kt` | 新建 | 歌单 DAO |
| `data/local/MusicDatabase.kt` | 修改 | 注册 DAO |
| `data/repository/MusicRepository.kt` | 修改 | 添加歌单 CRUD 方法 |
| `presentation/viewmodel/MusicViewModel.kt` | 修改 | 添加歌单 ViewModel 方法 |
| `presentation/ui/screens/playlist/PlaylistScreen.kt` | 新建 | 歌单页面 |
| `presentation/ui/components/PlaylistPickerDialog.kt` | 新建 | 歌单选择对话框 |
| `presentation/navigation/MusicNavHost.kt` | 修改 | 添加路由 |

## 7. 成功/失败提示

- 创建歌单成功：Toast "歌单已创建"
- 删除歌单成功：Toast "歌单已删除"
- 重命名成功：Toast "歌单已重命名"
- 添加到歌单成功：Toast "已添加到 {歌单名}"
- 移动歌曲成功：Toast "已移动到 {歌单名}"
- 歌单为空时删除：提示确认对话框
- 创建歌单名称为空：Toast "请输入歌单名称"
