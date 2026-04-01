# 逻辑测试用例：权限管理功能

## 功能概述

权限管理功能包括：存储权限（READ_EXTERNAL_STORAGE/READ_MEDIA_AUDIO）、通知权限（POST_NOTIFICATIONS）、WRITE_SETTINGS 权限（铃声设置）。

## 测试范围

- 测试层级：单元测试
- 测试对象：MusicViewModel 的权限处理逻辑

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 6 | 2 | 5 | 13 |

---

## 测试用例

### TC-PERM-001: hasStoragePermission - Android 13+

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-001 |
| 测试点描述 | Android 13+ 检查 READ_MEDIA_AUDIO 权限 |
| 输入 | Build.VERSION.SDK_INT >= 33, READ_MEDIA_AUDIO granted |
| 预期输出 | hasStoragePermission()=true |
| 测试类型 | 正常流程 |

---

### TC-PERM-002: hasStoragePermission - Android 13+ 无权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-002 |
| 测试点描述 | Android 13+ 无 READ_MEDIA_AUDIO 权限 |
| 输入 | Build.VERSION.SDK_INT >= 33, READ_MEDIA_AUDIO not granted |
| 预期输出 | hasStoragePermission()=false |
| 测试类型 | 异常处理 |

---

### TC-PERM-003: hasStoragePermission - Android 13 以下

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-003 |
| 测试点描述 | Android 13 以下检查 READ_EXTERNAL_STORAGE 权限 |
| 输入 | Build.VERSION.SDK_INT < 33, READ_EXTERNAL_STORAGE granted |
| 预期输出 | hasStoragePermission()=true |
| 测试类型 | 正常流程 |

---

### TC-PERM-004: hasStoragePermission - Android 13 以下无权限

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-004 |
| 测试点描述 | Android 13 以下无 READ_EXTERNAL_STORAGE 权限 |
| 输入 | Build.VERSION.SDK_INT < 33, READ_EXTERNAL_STORAGE not granted |
| 预期输出 | hasStoragePermission()=false |
| 测试类型 | 异常处理 |

---

### TC-PERM-005: onPermissionGranted - 权限授予

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-005 |
| 测试点描述 | 权限授予后更新状态并绑定服务 |
| 输入 | 用户授予存储权限 |
| 预期输出 | hasPermission=true，bindService() |
| 测试类型 | 正常流程 |

---

### TC-PERM-006: onPermissionGranted - 已有歌曲跳过扫描

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-006 |
| 测试点描述 | 权限授予时如已有歌曲数据跳过首次扫描 |
| 输入 | hasSongs()=true |
| 预期输出 | 不调用 scanMusic() |
| 测试类型 | 边界条件 |

---

### TC-PERM-007: onPermissionGranted - 无歌曲执行扫描

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-007 |
| 测试点描述 | 权限授予时如无歌曲则执行首次扫描 |
| 输入 | hasSongs()=false |
| 预期输出 | 调用 scanMusic() |
| 测试类型 | 正常流程 |

---

### TC-PERM-008: onPermissionDenied - 权限拒绝

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-008 |
| 测试点描述 | 权限拒绝后更新状态 |
| 输入 | 用户拒绝存储权限 |
| 预期输出 | hasPermission=false |
| 测试类型 | 异常处理 |

---

### TC-PERM-009: showPermissionDeniedDialog - 显示对话框

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-009 |
| 测试点描述 | 显示权限解释对话框 |
| 输入 | showPermissionDeniedDialog("标题", "消息") |
| 预期输出 | showPermissionDeniedDialog=true, permissionDeniedTitle="标题", permissionDeniedMessage="消息" |
| 测试类型 | 正常流程 |

---

### TC-PERM-010: dismissPermissionDeniedDialog - 关闭对话框

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-010 |
| 测试点描述 | 关闭权限解释对话框 |
| 输入 | dismissPermissionDeniedDialog() |
| 预期输出 | showPermissionDeniedDialog=false |
| 测试类型 | 正常交互 |

---

### TC-PERM-011: 无权限页面 - 无权限显示

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-011 |
| 测试点描述 | 无存储权限时显示无权限页面 |
| 输入 | hasPermission=false |
| 预期输出 | NoPermissionContent 显示 |
| 测试类型 | 异常处理 |

---

### TC-PERM-012: hasLoadedSongs - 加载状态

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-012 |
| 测试点描述 | 歌曲数据加载完成前不显示内容 |
| 输入 | hasLoadedSongs=false |
| 预期输出 | MyMusicScreen 返回，不显示 Tab |
| 测试类型 | 边界条件 |

---

### TC-PERM-013: hasLoadedSongs - 加载完成显示

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-PERM-013 |
| 测试点描述 | 歌曲数据加载完成后正常显示 |
| 输入 | hasPermission=true, hasLoadedSongs=true |
| 预期输出 | MyMusicScreen 正常显示 |
| 测试类型 | 正常流程 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 6 |
| 边界条件 | 2 |
| 异常处理 | 5 |
| **总计** | 13 |
