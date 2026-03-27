# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

项目概述、技术栈、架构设计、功能列表、数据库结构、导航路由等项目本身相关的信息详见 `SPEC.md`。

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

## 规范维护

每次完成功能添加、逻辑修改或代码重构后，如果涉及：
- 新增/删除/修改了功能
- 新的页面或组件
- 新的库或技术方案
- 数据库结构变更

立即更新 SPEC.md 中对应的内容，保持与代码一致。

## 测试要求

每次编写功能代码时，必须同步编写对应的测试代码，不需要用户提醒：

- 功能实现 Task 完成后，**紧接着**添加测试 Task
- 测试 Task 放在功能 Task 之后、最终构建 Task 之前
- 测试覆盖：正常流程、边界条件、异常/错误处理

示例 Plan 结构：
```
## Task 3: 实现功能
- [ ] ...
- [ ] Commit

## Task 4: 编写测试
- [ ] 创建 XxxTest.kt
- [ ] 测试正常流程
- [ ] 测试边界条件
- [ ] Commit

## Task 5: 构建验证
- [ ] ./gradlew test
- [ ] Commit
```
