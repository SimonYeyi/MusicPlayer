# 删除歌曲 - 逻辑测试用例

## 统计

**逻辑测试用例统计：**

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 3 | 3 | 2 | 8 |

## 测试用例详情

### 正常流程

| 测试用例编号 | 测试点描述 | 输入 | 预期输出 | 测试类型 |
|-------------|-----------|------|---------|---------|
| TC-001 | 仅从数据库删除歌曲（不删除文件） | songId=100, deleteFile=false | SongDao.deleteSong(100) 被调用，歌曲记录从数据库移除，文件保留 | 正常流程 |
| TC-002 | 删除歌曲并触发文件删除请求（URI存在） | songId=100, deleteFile=true, URI存在 | PendingDeleteRequest 被设置，Activity 观察到此状态后弹出系统权限对话框 | 正常流程 |
| TC-003 | 删除当前播放歌曲后自动播放下一首 | 当前播放 songId=100, 播放队列有下一首 | deleteSong(100, false) 后，playNext() 被调用自动切换到下一首 | 正常流程 |

### 边界条件

| 测试用例编号 | 测试点描述 | 输入 | 预期输出 | 测试类型 |
|-------------|-----------|------|---------|---------|
| TC-004 | 删除数据库中不存在的歌曲ID | songId=99999 (不存在) | SongDao.deleteSong(99999) 执行，返回删除行数=0，无异常抛出 | 边界条件 |
| TC-005 | deleteFile=true 但 URI 为空时的处理 | songId=100, URI=null, deleteFile=true | 降级为仅数据库删除，不设置 PendingDeleteRequest | 边界条件 |
| TC-006 | 删除歌曲后刷新歌曲列表 | 删除 songId=100 成功 | UI 列表刷新，歌曲从列表中移除 | 边界条件 |

### 异常处理

| 测试用例编号 | 测试点描述 | 输入 | 预期输出 | 测试类型 |
|-------------|-----------|------|---------|---------|
| TC-007 | 数据库删除操作抛出异常 | SongDao.deleteSong 抛出 SQLException | 异常向上传播，UI 显示错误提示 | 异常处理 |
| TC-008 | PendingDeleteRequest 时 Activity 未绑定服务 | deleteFile=true, URI存在，但 Activity 未绑定 | _pendingDeleteRequest 设置后无人消费，内存泄漏（需确保 Activity 正确绑定） | 异常处理 |
