# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

项目主体相关的信息： @README.md

## 构建命令

```bash
# 构建 debug APK
./gradlew assembleDebug

# 构建 release APK
./gradlew assembleRelease

# 运行所有测试
./gradlew test

# 仅运行单元测试
./gradlew testDebugUnitTest

# 运行 Android instrumented 测试
./gradlew connectedDebugAndroidTest

# 清理并重新构建
./gradlew clean assembleDebug

# 检查依赖更新
./gradlew dependencyUpdates
```

## 测试

- **单元测试**：`src/test/java/com/musicplayer/` - 领域模型、FormatUtils、ViewModel 逻辑
- **Android 测试**：`src/androidTest/java/com/musicplayer/` - UI 组件、页面、数据库测试
- 测试依赖：JUnit、MockK、Turbine（Flow 测试）、Room testing、Hilt testing
