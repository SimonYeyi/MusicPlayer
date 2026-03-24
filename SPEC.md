# 音乐播放器 - 项目规范

## 1. 项目概述

- **项目名称**: MusicPlayer
- **项目类型**: Android原生应用
- **核心功能**: 本地音乐播放器，支持扫描、播放、列表管理、后台播放等基础功能

## 2. 技术栈

- **语言**: Kotlin 1.9.x
- **最小SDK**: 24 (Android 7.0)
- **目标SDK**: 34 (Android 14)
- **编译SDK**: 34
- **架构**: MVVM + Clean Architecture
- **UI框架**: Jetpack Compose + Material Design 3
- **依赖注入**: Hilt
- **异步**: Kotlin Coroutines + Flow
- **音频播放**: MediaPlayer / ExoPlayer
- **本地存储**: Room Database
- **图片加载**: Coil

## 3. 功能列表

### 3.1 音乐扫描
- 扫描设备本地音乐文件（MP3格式）
- 读取歌曲标题、艺术家、专辑、时长等元数据
- 自动刷新音乐库

### 3.2 播放控制
- 播放/暂停
- 上一首/下一首
- 进度条拖动
- 随机播放/列表循环/单曲循环

### 3.3 播放列表
- 显示所有歌曲列表
- 显示正在播放列表
- 歌曲搜索

### 3.4 后台播放
- 前台服务保持播放
- 通知栏显示播放控制和歌曲信息

### 3.5 UI界面
- 主界面：歌曲列表
- 底部播放栏：显示当前歌曲和播放控制
- 播放详情页：专辑封面、大进度条、更多控制

## 4. UI/UX 设计方向

- **视觉风格**: Material Design 3，简洁现代
- **配色方案**: 深色主题为主，紫色/蓝色点缀
- **布局方式**: 单页面+底部弹窗，Tab切换（歌曲/歌手/专辑）
- **动画**: 简单的过渡动画

## 5. 项目结构

```
app/
├── src/main/
│   ├── java/com/musicplayer/
│   │   ├── di/           # Hilt模块
│   │   ├── data/         # 数据层
│   │   │   ├── local/    # Room数据库
│   │   │   └── repository/
│   │   ├── domain/       # 领域层
│   │   │   ├── model/
│   │   │   └── usecase/
│   │   ├── presentation/ # UI层
│   │   │   ├── ui/
│   │   │   └── viewmodel/
│   │   ├── service/      # 播放服务
│   │   └── util/
│   └── res/
└── build.gradle
```
