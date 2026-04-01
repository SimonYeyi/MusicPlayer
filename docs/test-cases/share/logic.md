# 逻辑测试用例：分享功能

## 功能概述

分享功能：将歌曲分享给其他应用或联系人。

## 测试范围

- 测试层级：单元测试
- 测试对象：ShareHelper、MusicViewModel 的分享逻辑

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 5 | 0 | 4 | 9 |

---

## 测试用例

### TC-SHARE-001: shareSong - 正常分享

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-001 |
| 测试点描述 | shareSong 应创建分享 Intent |
| 输入 | song.uri != null |
| 预期输出 | Intent(Intent.ACTION_SEND) 被创建，type="audio/*" |
| 测试类型 | 正常流程 |

---

### TC-SHARE-002: shareSong - URI 为 null

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-002 |
| 测试点描述 | URI 为 null 时分享失败 |
| 输入 | song.uri = null |
| 预期输出 | 返回 false，不创建 Intent |
| 测试类型 | 异常处理 |

---

### TC-SHARE-003: shareSong - Intent 附加数据

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-003 |
| 测试点描述 | Intent 应包含正确的附加数据 |
| 输入 | song.title="测试歌曲", song.artist="测试艺术家" |
| 预期输出 | Intent.EXTRA_TEXT="分享歌曲：测试歌曲 - 测试艺术家"，EXTRA_STREAM=URI |
| 测试类型 | 正常流程 |

---

### TC-SHARE-004: shareSong - 创建选择器

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-004 |
| 测试点描述 | shareSong 应使用选择器让用户选择分享目标 |
| 输入 | 正常分享 |
| 预期输出 | Intent.createChooser(intent, "分享歌曲") |
| 测试类型 | 正常流程 |

---

### TC-SHARE-005: shareSong - 授权读权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-005 |
| 测试点描述 | 分享 Intent 应授权 URI 读权限 |
| 输入 | 正常分享 |
| 预期输出 | chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) |
| 测试类型 | 正常流程 |

---

### TC-SHARE-006: shareSong - 分享成功返回

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-006 |
| 测试点描述 | 分享成功返回 true |
| 输入 | startActivity 成功执行 |
| 预期输出 | 返回 true |
| 测试类型 | 正常流程 |

---

### TC-SHARE-007: shareSong - 分享失败处理

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-007 |
| 测试点描述 | 无可用应用时返回 false |
| 输入 | startActivity 抛出 ActivityNotFoundException |
| 预期输出 | 返回 false，捕获异常不崩溃 |
| 测试类型 | 异常处理 |

---

### TC-SHARE-008: onShareClick - 触发分享事件

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-008 |
| 测试点描述 | onShareClick 应触发分享事件流 |
| 输入 | song.uri != null |
| 预期输出 | _shareSongEvent emit song |
| 测试类型 | 正常流程 |

---

### TC-SHARE-009: onShareClick - URI 为 null

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-SHARE-009 |
| 测试点描述 | URI 为 null 时发送失败提示 |
| 输入 | song.uri = null |
| 预期输出 | _ringtoneToastMessage emit "分享失败" |
| 测试类型 | 异常处理 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 5 |
| 边界条件 | 0 |
| 异常处理 | 4 |
| **总计** | 9 |
