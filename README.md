# 音乐播放器 - 项目规范

## 1. 项目概述

- **项目名称**: MusicPlayer
- **项目类型**: Android 原生应用
- **核心功能**: 本地音乐播放器，支持扫描、播放、列表管理、后台播放、动态主题和铃声设置

## 2. 技术栈

- **语言**: Kotlin
- **最低 SDK**: 24（Android 7.0）
- **目标 SDK**: 36（Android 16）
- **编译 SDK**: 36
- **架构**: MVVM + Clean Architecture
- **UI 框架**: Jetpack Compose + Material Design 3
- **依赖注入**: Hilt + KSP
- **异步**: Kotlin Coroutines + Flow
- **音频播放**: AndroidX Media
- **本地存储**: Room Database
- **图片加载**: Coil

## 3. 功能列表

### 3.1 音乐扫描
- 扫描设备本地音乐文件
- 读取歌曲标题、艺术家、专辑、时长、专辑封面等元数据
- 支持删除歌曲（通过 MediaStore）

### 3.2 播放控制
- 播放/暂停
- 上一首/下一首
- 进度条拖动
- 随机播放/列表循环/单曲循环

### 3.3 播放列表
- 显示所有歌曲列表
- 创建、编辑、删除播放列表
- 歌曲搜索
- 收藏功能（Favorite）
- 最近播放记录（RecentPlay）

### 3.4 后台播放
- 前台服务保持播放
- 通知栏显示播放控制和歌曲信息

### 3.5 铃声设置
- 将歌曲设为手机铃声/通知/闹钟
- 使用 MediaStore API
- 需要 WRITE_SETTINGS 权限

### 3.6 主题切换
- 10 种心情主题：喜庆、哀伤、平静、活力、浪漫、神秘、自然、温暖、冷酷、清新
- 动态 ColorScheme，整应用颜色跟随主题变化

### 3.7 UI 界面
- 主界面：Tab 导航（本地音乐/收藏/最近播放/播放列表）
- 本地音乐：歌曲列表、歌手分组、搜索
- 收藏：收藏歌曲列表
- 最近播放：最近播放记录
- 播放列表：歌单列表，支持创建/编辑/删除
- 播放详情页：专辑封面、大进度条、播放控制
- 设置页：主题切换、歌手置顶管理
- 底部迷你播放器：当前歌曲、播放/暂停、下一首

## 4. UI/UX 设计方向

- **视觉风格**: Material Design 3，颜色随心情主题动态变化
- **配色方案**: 由用户选择的心情主题决定（10 种可选）
- **布局方式**: Tab 导航 + 页面导航 + 底部迷你播放器
- **权限处理**: 首次启动引导授权，音乐权限 + 通知权限（Android 13+）

## 5. 项目结构

```
app/
├── src/main/
│   ├── java/com/musicplayer/
│   │   ├── MusicPlayerApp.kt     # Application 类（Hilt 入口）
│   │   ├── di/                   # Hilt 模块
│   │   │   └── AppModule.kt
│   │   ├── data/                 # 数据层
│   │   │   ├── local/            # Room 数据库
│   │   │   │   ├── MusicDatabase.kt
│   │   │   │   ├── SongEntity.kt
│   │   │   │   ├── PlaylistEntity.kt
│   │   │   │   ├── PlaylistSongEntity.kt
│   │   │   │   ├── FavoriteEntity.kt
│   │   │   │   ├── RecentPlayEntity.kt
│   │   │   │   └── *Dao.kt
│   │   │   └── repository/
│   │   │       └── MusicRepository.kt
│   │   ├── domain/               # 领域层
│   │   │   └── model/
│   │   │       ├── Song.kt
│   │   │       ├── PlaybackState.kt
│   │   │       └── MoodTheme.kt
│   │   ├── presentation/         # UI 层
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── home/HomeScreen.kt
│   │   │   │   │   ├── musiclibrary/MusicLibraryScreen.kt
│   │   │   │   │   ├── mymusic/MyMusicScreen.kt
│   │   │   │   │   ├── playlist/PlaylistScreen.kt
│   │   │   │   │   ├── playdetail/PlayDetailScreen.kt
│   │   │   │   │   └── settings/SettingsScreen.kt
│   │   │   │   └── components/
│   │   │   │       ├── MiniPlayer.kt
│   │   │   │       ├── AlbumArt.kt
│   │   │   │       ├── MoodThemePicker.kt
│   │   │   │       └── PlaylistPickerDialog.kt
│   │   │   ├── viewmodel/
│   │   │   │   └── MusicViewModel.kt
│   │   │   └── navigation/
│   │   │       ├── MusicNavHost.kt
│   │   │       └── Screen.kt
│   │   ├── service/               # 播放服务
│   │   │   └── MusicPlaybackService.kt
│   │   └── util/
│   │       └── RingtoneHelper.kt
│   └── res/
└── build.gradle.kts
```

## 6. 数据库

Room 数据库包含以下实体：
- **SongEntity**: 歌曲信息
- **PlaylistEntity**: 播放列表
- **PlaylistSongEntity**: 播放列表与歌曲的关联
- **FavoriteEntity**: 收藏
- **RecentPlayEntity**: 最近播放

## 7. 导航

使用 Navigation Compose，路由如下：

| 路由 | 页面 | 说明 |
|------|------|------|
| `my_music` | 我的（首页） | 起始页面，含 4 个 Tab：本地音乐/收藏/最近播放/播放列表 |
| `play_detail` | 播放详情页 | 全屏页面，底部迷你播放器点击进入，支持滑动返回 |
| `playlist/{playlistId}/{playlistName}` | 播放列表详情 | 展示歌单内歌曲，支持嵌套跳转其他歌单 |
| `settings` | 设置页 | 主题切换、歌手置顶管理 |

### 页面跳转关系

```
my_music (主页面)
├── Tab: 本地音乐/收藏/最近播放/播放列表
├── 迷你播放器 → play_detail (底部点击)
├── 设置图标 → settings
└── 播放列表项 → playlist/{id}/{name}

playlist/{id}/{name}
├── 返回 → my_music
├── 歌曲 → 嵌套 playlist/{id}/{name}
└── 迷你播放器 → play_detail

play_detail
└── 返回 → my_music (带滑动动画)

settings
└── 返回 → my_music (可指定跳转 Tab)
```

### 页面转场动画
- `play_detail`：垂直滑动 + 渐变，300ms
- `playlist`：默认共享元素转场
- `settings`：默认滑动转场