# 逻辑测试用例：主题设置功能

## 功能概述

主题设置功能包括：心情主题切换（10种主题：喜庆、哀伤、平静、活力、浪漫、神秘、自然、温暖、冷酷、清新）。

## 测试范围

- 测试层级：单元测试
- 测试对象：MoodTheme 枚举、MusicViewModel 的主题设置逻辑

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 11 | 1 | 1 | 13 |

---

## 测试用例

### TC-THEME-001: MoodTheme 枚举值完整

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-001 |
| 测试点描述 | MoodTheme 应包含所有 10 种主题 |
| 输入 | MoodTheme.entries |
| 预期输出 | [HAPPY, SAD, CALM, ENERGETIC, ROMANTIC, MYSTERIOUS, NATURAL, WARM, COOL, FRESH] |
| 测试类型 | 正常流程 |

---

### TC-THEME-002: 主题属性验证 - HAPPY

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-002 |
| 测试点描述 | HAPPY 主题应有正确的显示名称和颜色 |
| 输入 | MoodTheme.HAPPY |
| 预期输出 | displayName="喜庆"，primaryColor=红色系，description="欢乐、庆祝、热情" |
| 测试类型 | 正常流程 |

---

### TC-THEME-003: 主题属性验证 - CALM

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-003 |
| 测试点描述 | CALM 主题应有正确的显示名称和颜色 |
| 输入 | MoodTheme.CALM |
| 预期输出 | displayName="平静"，primaryColor=蓝色系，description="放松、安宁、和谐" |
| 测试类型 | 正常流程 |

---

### TC-THEME-004: 主题属性验证 - 所有主题

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-004 |
| 测试点描述 | 所有主题都应有 displayName 和颜色属性 |
| 输入 | 所有 MoodTheme 枚举值 |
| 预期输出 | 每个主题都有 displayName、primaryColor、secondaryColor、backgroundColor、surfaceColor、onPrimaryColor、description |
| 测试类型 | 正常流程 |

---

### TC-THEME-005: getAllMoodThemes 返回所有主题

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-005 |
| 测试点描述 | getAllMoodThemes 应返回 10 种主题列表 |
| 输入 | getAllMoodThemes() |
| 预期输出 | 返回包含所有 MoodTheme 的列表 |
| 测试类型 | 正常流程 |

---

### TC-THEME-006: 主题切换 - 默认主题

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-006 |
| 测试点描述 | 应用首次安装默认主题为 CALM |
| 输入 | 首次启动应用 |
| 预期输出 | currentTheme=CALM |
| 测试类型 | 正常流程 |

---

### TC-THEME-007: 主题切换 - setTheme

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-007 |
| 测试点描述 | setTheme 应更新当前主题 |
| 输入 | setTheme(HAPPY) |
| 预期输出 | currentTheme=HAPPY |
| 测试类型 | 正常流程 |

---

### TC-THEME-008: 主题切换 - 持久化

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-008 |
| 测试点描述 | 主题应保存到 SharedPreferences |
| 输入 | setTheme(HAPPY) |
| 预期输出 | SharedPreferences 保存 KEY_THEME=HAPPY |
| 测试类型 | 正常流程 |

---

### TC-THEME-009: 主题持久化恢复

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-009 |
| 测试点描述 | 应用重启后应从 SharedPreferences 恢复主题 |
| 输入 | SharedPreferences 保存 HAPPY |
| 预期输出 | savedTheme=HAPPY，currentTheme 恢复为 HAPPY |
| 测试类型 | 正常流程 |

---

### TC-THEME-010: 主题持久化恢复 - 无效值

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-010 |
| 测试点描述 | 保存的主题名称无效时回退到 CALM |
| 输入 | SharedPreferences 保存 "INVALID_THEME" |
| 预期输出 | savedTheme 回退为 MoodTheme.CALM |
| 测试类型 | 异常处理 |

---

### TC-THEME-011: 主题切换 - 打开选择器

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-011 |
| 测试点描述 | toggleThemePicker 切换主题选择器显示状态 |
| 输入 | showThemePicker=false，调用 toggleThemePicker |
| 预期输出 | showThemePicker=true |
| 输入 | showThemePicker=true，调用 toggleThemePicker |
| 预期输出 | showThemePicker=false |
| 测试类型 | 正常流程 |

---

### TC-THEME-012: 主题切换 - 隐藏选择器

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-012 |
| 测试点描述 | hideThemePicker 隐藏选择器 |
| 输入 | showThemePicker=true，调用 hideThemePicker |
| 预期输出 | showThemePicker=false |
| 测试类型 | 正常交互 |

---

### TC-THEME-013: 主题颜色包含透明度

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-THEME-013 |
| 测试点描述 | 主题的 backgroundColor 和 surfaceColor 应有适当透明度 |
| 输入 | MoodTheme.CALM |
| 预期输出 | backgroundColor 和 surfaceColor 可用于 UI 背景 |
| 测试类型 | 边界条件 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 11 |
| 边界条件 | 1 |
| 异常处理 | 1 |
| **总计** | 13 |
