# 逻辑测试用例：音乐库功能

## 功能概述

音乐库页面（MusicLibraryScreen）展示本地音乐的分类入口，包括：歌曲、专辑、歌手、文件夹、最近播放、收藏共 6 个分类类别。

## 测试范围

- 测试层级：单元测试
- 测试对象：LibraryCategory 数据类、libraryCategories 列表、MusicLibraryScreen 组件逻辑

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 5 | 2 | 1 | 8 |

---

## 测试用例

### TC-001: LibraryCategory 数据类结构正确性

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-001 |
| 测试点描述 | LibraryCategory 数据类应正确包含 icon、title、subtitle 字段 |
| 输入 | LibraryCategory(icon=Icons.Default.MusicNote, title="歌曲", subtitle="本地音乐") |
| 预期输出 | 实例包含正确的 icon、title、subtitle 值 |
| 测试类型 | 正常流程 |

---

### TC-002: libraryCategories 列表包含 6 个分类

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-002 |
| 测试点描述 | libraryCategories 列表应包含全部 6 个预定义分类 |
| 输入 | libraryCategories |
| 预期输出 | 列表大小为 6，包含：歌曲、专辑、歌手、文件夹、最近播放、收藏 |
| 测试类型 | 正常流程 |

---

### TC-003: libraryCategories 分类顺序正确

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-003 |
| 测试点描述 | libraryCategories 列表应按预期顺序排列 |
| 输入 | libraryCategories.map { it.title } |
| 预期输出 | ["歌曲", "专辑", "歌手", "文件夹", "最近播放", "收藏"] |
| 测试类型 | 正常流程 |

---

### TC-004: MusicLibraryScreen 默认参数正确

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-004 |
| 测试点描述 | MusicLibraryScreen 的 onNavigateToPlayDetail 参数应有默认值 |
| 输入 | MusicLibraryScreen() 不传参数 |
| 预期输出 | onNavigateToPlayDetail 使用默认值 {} |
| 测试类型 | 正常流程 |

---

### TC-005: MusicLibraryScreen 点击"歌曲"分类触发导航回调

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-005 |
| 测试点描述 | 点击"歌曲"分类时应触发 onNavigateToPlayDetail 回调 |
| 输入 | category.title = "歌曲"，点击该分类 |
| 预期输出 | onNavigateToPlayDetail 被调用一次 |
| 测试类型 | 正常流程 |

---

### TC-006: MusicLibraryScreen 点击非歌曲分类不触发导航

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-006 |
| 测试点描述 | 点击"专辑"、"歌手"等非歌曲分类时不应触发 onNavigateToPlayDetail |
| 输入 | category.title = "专辑"，点击该分类 |
| 预期输出 | onNavigateToPlayDetail 不被调用 |
| 测试类型 | 边界条件 |

---

### TC-007: libraryCategories 列表为空时

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-007 |
| 测试点描述 | libraryCategories 列表为空时 MusicLibraryScreen 应正常渲染空列表 |
| 输入 | libraryCategories = [] |
| 预期输出 | LazyColumn 渲染空列表，不崩溃 |
| 测试类型 | 异常处理 |

---

### TC-008: LibraryCategoryItem 组件参数传递正确

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-008 |
| 测试点描述 | LibraryCategoryItem 应正确接收并使用 category 和 onClick 参数 |
| 输入 | LibraryCategoryItem(category=LibraryCategory(...), onClick={}) |
| 预期输出 | 组件正确显示 category 的 icon、title、subtitle，点击触发 onClick |
| 测试类型 | 正常流程 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 5 |
| 边界条件 | 2 |
| 异常处理 | 1 |
| **总计** | 8 |
