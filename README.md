# AI Music Player

本地音乐播放器，支持音乐扫描、播放控制、歌单管理、收藏管理、最近播放、设为铃声等功能。

## 功能清单

### 音乐播放
- 扫描本地音乐文件
- 播放控制：播放、暂停、上一曲、下一曲
- 进度拖拽
- 播放模式切换：列表循环、随机播放、单曲循环、关闭
- 播放队列管理
- 专辑封面显示与旋转动画
- 音频设备断开/重连自动暂停/恢复

### 歌单管理
- 创建、删除、重命名歌单
- 添加歌曲到歌单
- 从歌单移除歌曲
- 批量移动歌曲到其他歌单
- 歌单编辑模式（批量选择操作）

### 收藏管理
- 收藏/取消收藏歌曲
- 收藏列表展示

### 最近播放
- 自动记录播放历史
- 清除播放历史
- 从最近播放移除歌曲

### 歌手置顶
- 长按歌手名称置顶/取消置顶
- 置顶歌手优先显示在列表顶部

### 设为铃声
- 将歌曲设为来电铃声、闹钟铃声、通知铃声

### 主题设置
- 心情主题切换

### 设置
- 重新扫描音乐
- 查看本地音乐数量
- 跳转至收藏、最近播放

## 页面详情

### 页面层级关系与路由

```
App
└── 我的音乐（my_music）[主入口]
    ├── 播放详情（play_detail）
    ├── 设置（settings）
    └── 歌单详情（playlist/{playlistId}/{playlistName}）
```

### 页面说明

**我的音乐（my_music）**
- 主入口页面，包含 4 个 Tab：本地音乐、收藏、最近播放、歌单
- 支持搜索歌曲
- 歌曲按歌手分组，支持置顶歌手

**播放详情（play_detail）**
- 全屏播放界面
- 显示专辑封面（旋转动画）、歌曲名、艺术家
- 进度条控制
- 播放控制按钮
- 播放队列（底部弹窗）

**歌单详情（playlist/{playlistId}/{playlistName}）**
- 显示歌单内歌曲列表
- 支持编辑模式批量操作
- 支持从歌单移动/移除歌曲

**设置（settings）**
- 主题设置
- 播放模式切换
- 音乐管理（重新扫描）
- 播放历史入口

## 权限说明

| 权限 | 作用 | 类型 |
|------|------|------|
| READ_EXTERNAL_STORAGE | 读取外部存储中的音乐文件 | 动态权限（maxSdkVersion=32） |
| READ_MEDIA_AUDIO | 读取音频文件（Android 13+） | 动态权限 |
| DELETE_PERMISSION | 删除音乐文件 | 系统权限 |
| FOREGROUND_SERVICE | 前台服务运行 | 安装时授予 |
| FOREGROUND_SERVICE_MEDIA_PLAYBACK | 前台媒体播放服务 | 安装时授予 |
| POST_NOTIFICATIONS | 发送播放通知 | 动态权限 |
| WAKE_LOCK | 保持 CPU 运行 | 安装时授予 |
| WRITE_SETTINGS | 写入系统设置（设为铃声） | 特殊权限 |

## 项目架构

```
app/src/main/java/com/musicplayer/
├── data/
│   ├── local/              # 本地数据层
│   │   ├── MusicDatabase.kt       # Room 数据库
│   │   ├── SongEntity.kt          # 歌曲实体
│   │   ├── PlaylistEntity.kt      # 歌单实体
│   │   ├── PlaylistSongEntity.kt  # 歌单-歌曲关联实体
│   │   ├── FavoriteEntity.kt      # 收藏实体
│   │   ├── RecentPlayEntity.kt   # 最近播放实体
│   │   └── *Dao.kt
│   └── repository/
│       └── MusicRepository.kt     # 音乐仓储
├── di/
│   └── AppModule.kt              # Hilt 依赖注入模块
├── domain/
│   └── model/
│       ├── Song.kt               # 歌曲领域模型
│       ├── PlaybackState.kt     # 播放状态
│       ├── MoodTheme.kt          # 心情主题枚举
│       └── PlayMode.kt          # 播放模式枚举
├── presentation/
│   ├── navigation/
│   │   ├── Screen.kt            # 路由密封类
│   │   └── MusicNavHost.kt     # 导航宿主
│   ├── ui/
│   │   ├── MainActivity.kt
│   │   ├── components/          # 通用组件
│   │   └── screens/
│   │       ├── mymusic/
│   │       │   └── MyMusicScreen.kt
│   │       ├── playdetail/
│   │       │   └── PlayDetailScreen.kt
│   │       ├── playlist/
│   │       │   └── PlaylistScreen.kt
│   │       └── settings/
│   │           └── SettingsScreen.kt
│   └── viewmodel/
│       └── MusicViewModel.kt
├── service/
│   └── MusicPlaybackService.kt  # 媒体播放服务
└── util/
    ├── RingtoneHelper.kt        # 铃声设置工具类
    └── ShareHelper.kt           # 分享工具类
```

## 数据库结构

### MusicDatabase

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| songs | 歌曲表 | id (PK), title, artist, album, duration, uri, albumArtUri |
| playlists | 歌单表 | id (PK, auto), name, createdAt, songCount |
| playlist_songs | 歌单-歌曲关联表 | playlistId, songId (复合 PK) |
| favorites | 收藏表 | songId (PK) |
| recent_plays | 最近播放表 | songId (PK), playedAt |

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| SDK | compileSdk=36, targetSdk=36, minSdk=24 |
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 导航 | Jetpack Navigation Compose |
| 图片加载 | Coil |
| 异步 | Kotlin Coroutines + Flow |
| 媒体播放 | MediaPlayer + MediaSession |
| 测试 | JUnit, MockK, Turbine, Room Testing, Hilt Testing |
