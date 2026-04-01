# 逻辑测试用例：铃声设置功能

## 功能概述

铃声设置功能包括：将歌曲设为来电铃声、闹钟铃声、通知铃声。

## 测试范围

- 测试层级：单元测试
- 测试对象：RingtoneHelper、MusicViewModel 的铃声设置逻辑

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 10 | 0 | 7 | 17 |

---

## 测试用例

### TC-RING-001: 铃声类型枚举完整

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-001 |
| 测试点描述 | RingtoneType 应包含所有铃声类型 |
| 输入 | RingtoneType.entries |
| 预期输出 | [RINGTONE, NOTIFICATION, ALARM]，每个有 title 和 androidType |
| 测试类型 | 正常流程 |

---

### TC-RING-002: canWriteSettings - 有权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-002 |
| 测试点描述 | 有 WRITE_SETTINGS 权限时返回 true |
| 输入 | Settings.System.canWrite(context)=true |
| 预期输出 | canWriteSettings(context)=true |
| 测试类型 | 正常流程 |

---

### TC-RING-003: canWriteSettings - 无权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-003 |
| 测试点描述 | 无 WRITE_SETTINGS 权限时返回 false |
| 输入 | Settings.System.canWrite(context)=false |
| 预期输出 | canWriteSettings(context)=false |
| 测试类型 | 异常处理 |

---

### TC-RING-004: getRingtoneTypeIntent - 生成跳转 Intent

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-004 |
| 测试点描述 | getRingtoneTypeIntent 应生成正确的设置跳转 Intent |
| 输入 | RingtoneType.RINGTONE |
| 预期输出 | Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS) |
| 测试类型 | 正常流程 |

---

### TC-RING-005: getRingtonePickerIntent - 生成选择器 Intent

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-005 |
| 测试点描述 | getRingtonePickerIntent 应生成铃声选择器 Intent |
| 输入 | RingtoneType.RINGTONE, songUri |
| 预期输出 | Intent(RingtoneManager.ACTION_RINGTONE_PICKER) 带有正确参数 |
| 测试类型 | 正常流程 |

---

### TC-RING-006: setRingtone - 正常设置

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-006 |
| 测试点描述 | setRingtone 应成功设置铃声 |
| 输入 | type=RINGTONE, songUri=有效 URI |
| 预期输出 | RingtoneManager.setActualDefaultRingtoneUri 被调用，返回 true |
| 测试类型 | 正常流程 |

---

### TC-RING-007: setRingtone - URI 为 null

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-007 |
| 测试点描述 | URI 为 null 时设置失败 |
| 输入 | type=RINGTONE, songUri=null |
| 预期输出 | 返回 false，不调用 setActualDefaultRingtoneUri |
| 测试类型 | 异常处理 |

---

### TC-RING-008: setRingtone - 权限不足

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-008 |
| 测试点描述 | 权限不足时设置失败 |
| 输入 | 无 WRITE_SETTINGS 权限，调用 setRingtone |
| 预期输出 | 抛出异常或返回 false |
| 测试类型 | 异常处理 |

---

### TC-RING-009: 铃声设置流程 - 有权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-009 |
| 测试点描述 | 有权限时直接显示类型选择对话框 |
| 输入 | canWriteSettings=true, songId=100 |
| 预期输出 | showRingtoneTypeDialog=true |
| 测试类型 | 正常流程 |

---

### TC-RING-010: 铃声设置流程 - 无权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-010 |
| 测试点描述 | 无权限时显示权限申请对话框 |
| 输入 | canWriteSettings=false, songId=100 |
| 预期输出 | showRingtonePermissionDialog=true |
| 测试类型 | 异常处理 |

---

### TC-RING-011: 铃声类型选择 - 来电铃声

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-011 |
| 测试点描述 | 选择来电铃声类型 |
| 输入 | onRingtoneTypeSelected(RINGTONE) |
| 预期输出 | setRingtone(context, RINGTONE, songUri) |
| 测试类型 | 正常流程 |

---

### TC-RING-012: 铃声类型选择 - 通知铃声

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-012 |
| 测试点描述 | 选择通知铃声类型 |
| 输入 | onRingtoneTypeSelected(NOTIFICATION) |
| 预期输出 | setRingtone(context, NOTIFICATION, songUri) |
| 测试类型 | 正常流程 |

---

### TC-RING-013: 铃声类型选择 - 闹钟铃声

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-013 |
| 测试点描述 | 选择闹钟铃声类型 |
| 输入 | onRingtoneTypeSelected(ALARM) |
| 预期输出 | setRingtone(context, ALARM, songUri) |
| 测试类型 | 正常流程 |

---

### TC-RING-014: 铃声设置成功提示

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-014 |
| 测试点描述 | 设置成功后显示 Toast 提示 |
| 输入 | setRingtone 返回 true |
| 预期输出 | _ringtoneToastMessage emit "已设为{type.title}" |
| 测试类型 | 正常流程 |

---

### TC-RING-015: 铃声设置失败提示

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-015 |
| 测试点描述 | 设置失败后显示错误提示 |
| 输入 | setRingtone 返回 false |
| 预期输出 | _ringtoneToastMessage emit "设置铃声失败" |
| 测试类型 | 异常处理 |

---

### TC-RING-016: 关闭权限对话框

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-016 |
| 测试点描述 | 关闭铃声设置对话框 |
| 输入 | onDismissRingtoneDialog() |
| 预期输出 | showRingtoneTypeDialog=false, showRingtonePermissionDialog=false, pendingRingtoneSongId=null |
| 测试类型 | 正常交互 |

---

### TC-RING-017: 歌曲信息获取失败

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-RING-017 |
| 测试点描述 | 获取歌曲信息失败时显示提示 |
| 输入 | getSongById(songId)=null |
| 预期输出 | _ringtoneToastMessage emit "歌曲信息获取失败" |
| 测试类型 | 异常处理 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 10 |
| 边界条件 | 0 |
| 异常处理 | 7 |
| **总计** | 17 |
