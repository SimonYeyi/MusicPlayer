# AI Music Player

本地音乐播放器，支持歌曲管理、歌单、收藏、播放历史、心情主题等功能。

## 功能清单

### 音乐管理
- 扫描并加载本地音频文件
- 按歌手分组展示歌曲
- 歌手置顶功能
- 歌曲搜索
- 删除本地歌曲（可选同时删除文件）

### 播放功能
- 顺序播放、随机播放、单曲循环
- 后台播放（前台服务）
- 通知栏控制
- 迷你播放器

### 收藏与历史
- 收藏歌曲
- 最近播放记录
- 播放历史管理

### 歌单管理
- 创建、删除、重命名歌单
- 添加/移除歌曲到歌单
- 歌单内歌曲管理

### 铃声设置
- 设置歌曲为来电铃声、闹钟铃声、通知铃声

### 主题
- 心情主题切换（多种颜色主题）

### 分享
- 分享歌曲给其他应用

## 页面与路由

```
App
└── 我的音乐（/my_music）
    ├── 本地音乐（Tab 0）
    ├── 收藏（Tab 1）
    ├── 最近播放（Tab 2）
    └── 歌单（Tab 3）
        └── 歌单详情（/playlist/{playlistId}/{playlistName}）
    ├── 播放详情（/play_detail）
    └── 设置（/settings）
```

## 权限说明

| 权限 | 作用 | 类型 |
|------|------|------|
| READ_EXTERNAL_STORAGE | 读取存储中的音频文件（Android 12及以下） | 动态权限 |
| READ_MEDIA_AUDIO | 读取存储中的音频文件（Android 13+） | 动态权限 |
| POST_NOTIFICATIONS | 显示播放通知 | 动态权限（Android 13+） |
| DELETE_PERMISSION | 删除音频文件 | 系统权限 |
| FOREGROUND_SERVICE | 前台播放服务 | 声明权限 |
| FOREGROUND_SERVICE_MEDIA_PLAYBACK | 媒体播放前台服务 | 声明权限 |
| WAKE_LOCK | 防止播放时休眠 | 声明权限 |
| WRITE_SETTINGS | 写入系统设置（铃声） | 系统权限 |

## 架构概览

```
app/src/main/java/com/musicplayer/
├── data/
│   ├── local/          # Room 数据库（Entity、Dao）
│   └── repository/    # 数据仓库
├── di/                # Hilt 依赖注入模块
├── domain/
│   └── model/         # 领域模型
├── presentation/
│   ├── navigation/    # 导航配置
│   ├── ui/
│   │   ├── components/ # 可复用 UI 组件
│   │   └── screens/   # 页面
│   └── viewmodel/     # ViewModel
├── service/           # 音乐播放服务
└── util/              # 工具类
```

## 数据库结构

### SongEntity（歌曲）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 歌曲ID（媒体库ID） |
| title | String | 歌曲标题 |
| artist | String | 艺术家 |
| album | String | 专辑 |
| duration | Long | 时长（毫秒） |
| uri | String | 文件URI |
| albumArtUri | String? | 专辑封面URI |

### PlaylistEntity（歌单）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 歌单ID（自增） |
| name | String | 歌单名称 |
| createdAt | Long | 创建时间 |
| songCount | Int | 歌曲数量 |

### PlaylistSongEntity（歌单歌曲关联）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID（自增） |
| playlistId | Long | 歌单ID |
| songId | Long | 歌曲ID |
| addedAt | Long | 添加时间 |

### FavoriteEntity（收藏）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID（自增） |
| songId | Long | 歌曲ID |
| addedAt | Long | 收藏时间 |

### RecentPlayEntity（最近播放）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | ID（自增） |
| songId | Long | 歌曲ID |
| playedAt | Long | 播放时间 |

## 关键技术栈

| 类别 | 技术 |
|------|------|
| 平台 | Android |
| 语言 | Kotlin |
| UI框架 | Jetpack Compose |
| 最低SDK | 24（Android 7.0） |
| 目标SDK | 36 |
| 架构 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 导航 | Navigation Compose |
| 图片加载 | Coil |
| 媒体播放 | Media3 |
| 异步 | Kotlin Coroutines + Flow |
