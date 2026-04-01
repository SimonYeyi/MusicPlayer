# 逻辑测试用例：音乐播放功能

## 功能概述

音乐播放功能包括：扫描本地音乐文件、播放控制（播放/暂停/上一曲/下一曲）、进度拖拽、播放模式切换（列表循环/随机播放/单曲循环/关闭）、队列管理、专辑封面显示与旋转动画、音频设备断开/重连自动暂停/恢复。

## 测试范围

- 测试层级：单元测试
- 测试对象：MusicPlaybackService 的播放逻辑、MusicRepository 的音乐扫描逻辑、PlaybackState、PlayMode

---

## 测试用例统计

| 正常流程 | 边界条件 | 异常处理 | 总计 |
|---------|---------|---------|------|
| 25 | 7 | 8 | 40 |

---

## 测试用例

### TC-001: PlaybackState 默认值正确

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-001 |
| 测试点描述 | PlaybackState 数据类的默认值应正确初始化 |
| 输入 | PlaybackState() |
| 预期输出 | currentSong=null, isPlaying=false, currentPosition=0L, duration=0L, playMode=LIST_LOOP |
| 测试类型 | 正常流程 |

---

### TC-002: PlayMode 枚举值完整

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-002 |
| 测试点描述 | PlayMode 枚举应包含所有四种播放模式 |
| 输入 | PlayMode.entries |
| 预期输出 | [OFF, LIST_LOOP, SHUFFLE, ONE_LOOP]，每个模式有正确的 displayName |
| 测试类型 | 正常流程 |

---

### TC-003: 播放模式切换 - LIST_LOOP -> SHUFFLE

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-003 |
| 测试点描述 | 播放模式应在 LIST_LOOP、SHUFFLE、ONE_LOOP、OFF 之间循环切换 |
| 输入 | 当前 playMode = LIST_LOOP，调用 togglePlayMode() |
| 预期输出 | playMode = SHUFFLE |
| 测试类型 | 正常流程 |

---

### TC-004: 播放模式切换 - SHUFFLE -> ONE_LOOP

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-004 |
| 测试点描述 | 从随机播放模式切换到单曲循环模式 |
| 输入 | 当前 playMode = SHUFFLE，调用 togglePlayMode() |
| 预期输出 | playMode = ONE_LOOP |
| 测试类型 | 正常流程 |

---

### TC-005: 播放模式切换 - ONE_LOOP -> OFF

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-005 |
| 测试点描述 | 从单曲循环模式切换到关闭模式 |
| 输入 | 当前 playMode = ONE_LOOP，调用 togglePlayMode() |
| 预期输出 | playMode = OFF |
| 测试类型 | 正常流程 |

---

### TC-006: 播放模式切换 - OFF -> LIST_LOOP

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-006 |
| 测试点描述 | 从关闭模式循环切换回列表循环模式 |
| 输入 | 当前 playMode = OFF，调用 togglePlayMode() |
| 预期输出 | playMode = LIST_LOOP |
| 测试类型 | 正常流程 |

---

### TC-007: 列表循环 - 播放完最后一首后回到第一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-007 |
| 测试点描述 | 列表循环模式下，当前歌曲播放完成后应自动播放下一首，播完最后一首后回到第一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=3（播放D），歌曲D播放完成 |
| 预期输出 | currentIndex=0，自动播放 A |
| 测试类型 | 正常流程 |

---

### TC-008: 单曲循环 - 播放完成后重新播放同一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-008 |
| 测试点描述 | 单曲循环模式下，当前歌曲播放完成后应从头重新播放 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=1（播放B），B 播放完成 |
| 预期输出 | currentIndex=1，自动重新播放 B，position=0 |
| 测试类型 | 正常流程 |

---

### TC-009: 关闭模式 - 播放完最后一首停止

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-009 |
| 测试点描述 | 关闭模式下，播放完最后一首后应停止播放 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=3（播放D），D 播放完成 |
| 预期输出 | isPlaying=false，停止在 currentIndex=3 |
| 测试类型 | 正常流程 |

---

### TC-010: 关闭模式 - 播放完非最后一首继续下一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-010 |
| 测试点描述 | 关闭模式下，播放完非最后一首歌曲后应继续播放下一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=1（播放B），B 播放完成 |
| 预期输出 | currentIndex=2，自动播放 C |
| 测试类型 | 正常流程 |

---

### TC-011: setPlaylist - 设置新播放列表

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-011 |
| 测试点描述 | setPlaylist 应正确设置当前播放列表和播放位置 |
| 输入 | 歌曲列表 [A,B,C,D]，startSong=B |
| 预期输出 | _currentPlaylist=[A,B,C,D]，currentIndex=1 |
| 测试类型 | 正常流程 |

---

### TC-012: setPlaylist - startSong 为 null 时播放第一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-012 |
| 测试点描述 | setPlaylist 时 startSong 为 null 应播放列表第一首 |
| 输入 | 歌曲列表 [A,B,C,D]，startSong=null |
| 预期输出 | currentIndex=0，currentSong=A |
| 测试类型 | 边界条件 |

---

### TC-013: setPlaylist - 随机模式重置队列

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-013 |
| 测试点描述 | 随机模式下设置新播放列表时应重新初始化随机队列 |
| 输入 | 当前 playMode=SHUFFLE，shuffleQueue=[0,2,1,3]，调用 setPlaylist([A,B,C,D], startSong=C) |
| 预期输出 | shuffleQueue=[2]，shuffleIndex=0（只含 C） |
| 测试类型 | 正常流程 |

---

### TC-014: playSong - 播放指定歌曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-014 |
| 测试点描述 | playSong 应播放指定的歌曲并更新 currentIndex |
| 输入 | 当前列表 [A,B,C,D]，currentIndex=0（播放A），调用 playSong(C) |
| 预期输出 | currentIndex=2，currentSong=C，开始播放 C |
| 测试类型 | 正常流程 |

---

### TC-015: playSong - 播放当前歌曲（暂停状态恢复）

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-015 |
| 测试点描述 | 当播放的歌曲是当前歌曲且处于暂停状态时应恢复播放 |
| 输入 | 当前歌曲 C 正在播放但暂停，调用 playSong(C) |
| 预期输出 | isPlaying=true，恢复播放 |
| 测试类型 | 正常流程 |

---

### TC-016: playSong - 播放当前歌曲（播放中不重复）

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-016 |
| 测试点描述 | 当播放的歌曲是当前歌曲且正在播放时不应重复播放 |
| 输入 | 当前歌曲 C 正在播放，调用 playSong(C) |
| 预期输出 | isPlaying=true，保持播放状态不变 |
| 测试类型 | 异常处理 |

---

### TC-017: playSong - 随机模式插入队列

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-017 |
| 测试点描述 | 随机模式下播放非当前歌曲应插入到随机队列当前位置之后 |
| 输入 | playMode=SHUFFLE，shuffleQueue=[0,1,2,3]，shuffleIndex=0（播放A），调用 playSong(C) |
| 预期输出 | shuffleQueue=[0,2,1,3]，shuffleIndex=1，currentIndex=2 |
| 测试类型 | 正常流程 |

---

### TC-018: playNext - 列表循环下一曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-018 |
| 测试点描述 | 列表循环模式下 playNext 应播放下一首，播完最后一首回到第一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=1（播放B），调用 playNext() |
| 预期输出 | currentIndex=2，播放 C |
| 测试类型 | 正常流程 |

---

### TC-019: playNext - 列表循环到最后回到第一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-019 |
| 测试点描述 | 列表循环模式下播完最后一首应回到第一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=3（播放D），调用 playNext() |
| 预期输出 | currentIndex=0，播放 A |
| 测试类型 | 正常流程 |

---

### TC-020: playNext - 随机模式下一曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-020 |
| 测试点描述 | 随机模式下 playNext 应从随机队列中选取下一首歌曲 |
| 输入 | playMode=SHUFFLE，shuffleQueue=[0,2,1,3]，shuffleIndex=0（播放A），调用 playNext() |
| 预期输出 | shuffleIndex=1，currentIndex=2，播放 C |
| 测试类型 | 正常流程 |

---

### TC-021: playPrevious - 列表循环上一曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-021 |
| 测试点描述 | 列表循环模式下 playPrevious 应播放上一首，第一首时回到最后一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=1（播放B），调用 playPrevious() |
| 预期输出 | currentIndex=0，播放 A |
| 测试类型 | 正常流程 |

---

### TC-022: playPrevious - 列表循环到最后一首

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-022 |
| 测试点描述 | 列表循环模式下在第一首时点击上一曲应回到最后一首 |
| 输入 | 播放列表 [A,B,C,D]，currentIndex=0（播放A），调用 playPrevious() |
| 预期输出 | currentIndex=3，播放 D |
| 测试类型 | 正常流程 |

---

### TC-023: playPrevious - 随机模式上一曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-023 |
| 测试点描述 | 随机模式下 playPrevious 应从随机队列中选取上一首歌曲 |
| 输入 | playMode=SHUFFLE，shuffleQueue=[0,2,1,3]，shuffleIndex=1（播放C），调用 playPrevious() |
| 预期输出 | shuffleIndex=0，currentIndex=0，播放 A |
| 测试类型 | 正常流程 |

---

### TC-024: seekTo - 跳转到指定位置

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-024 |
| 测试点描述 | seekTo 应将播放进度跳转到指定位置 |
| 输入 | 当前播放位置 30000ms，调用 seekTo(60000) |
| 预期输出 | currentPosition=60000 |
| 测试类型 | 正常流程 |

---

### TC-025: seekTo - 边界值跳转

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-025 |
| 测试点描述 | seekTo 在边界值（0 和 duration）时应正确处理 |
| 输入 | duration=180000ms，调用 seekTo(0) 和 seekTo(200000) |
| 预期输出 | seekTo(0) -> position=0，seekTo(200000) -> position=180000（最大为 duration） |
| 测试类型 | 边界条件 |

---

### TC-026: syncPlaylist - 播放列表同步

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-026 |
| 测试点描述 | syncPlaylist 应在播放列表标识匹配时更新播放队列 |
| 输入 | currentPlaylistId="local"，调用 syncPlaylist("local", [A,B,C]) |
| 预期输出 | _currentPlaylist=[A,B,C] |
| 测试类型 | 正常流程 |

---

### TC-027: syncPlaylist - 列表标识不匹配不更新

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-027 |
| 测试点描述 | syncPlaylist 在播放列表标识不匹配时应忽略更新 |
| 输入 | currentPlaylistId="favorites"，调用 syncPlaylist("local", [A,B,C]) |
| 预期输出 | _currentPlaylist 保持不变 |
| 测试类型 | 异常处理 |

---

### TC-028: syncPlaylist - 当前歌曲保留

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-028 |
| 测试点描述 | syncPlaylist 同步过来的歌曲列表为空但播放器中有歌曲时应保留这首歌 |
| 输入 | currentPlaylistId="local"，currentSong=C，调用 syncPlaylist("local", []) |
| 预期输出 | _currentPlaylist=[C]，currentIndex 保持 |
| 测试类型 | 边界条件 |

---

### TC-029: 播放队列为空时 playNext/Previous

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-029 |
| 测试点描述 | 播放队列为空时 playNext 和 playPrevious 不应崩溃 |
| 输入 | _currentPlaylist=[]，调用 playNext() 或 playPrevious() |
| 预期输出 | 无操作，不崩溃 |
| 测试类型 | 异常处理 |

---

### TC-030: playSongAtIndex - 按索引播放

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-030 |
| 测试点描述 | playSongAtIndex 应播放指定索引位置的歌曲 |
| 输入 | 播放列表 [A,B,C,D]，调用 playSongAtIndex(2) |
| 预期输出 | currentIndex=2，播放 C |
| 测试类型 | 正常流程 |

---

### TC-031: playSongAtIndex - 索引越界处理

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-031 |
| 测试点描述 | playSongAtIndex 在索引越界时不应崩溃 |
| 输入 | 播放列表 [A,B,C,D]，调用 playSongAtIndex(10) 或 playSongAtIndex(-1) |
| 预期输出 | 无操作，不崩溃 |
| 测试类型 | 异常处理 |

---

### TC-032: 播放模式持久化

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-032 |
| 测试点描述 | 播放模式应持久化到 SharedPreferences |
| 输入 | playMode=SHUFFLE，切换后重启 Service |
| 预期输出 | 重启后 playMode=SHUFFLE |
| 测试类型 | 正常流程 |

---

### TC-033: scanMusicFromDevice - 扫描并保存歌曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-033 |
| 测试点描述 | scanMusicFromDevice 应从设备扫描音乐文件并保存到数据库 |
| 输入 | 设备上有音乐文件 |
| 预期输出 | 扫描完成后 songDao 中包含所有音乐文件对应的 SongEntity |
| 测试类型 | 正常流程 |

---

### TC-034: scanMusicFromDevice - 清空旧数据

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-034 |
| 测试点描述 | 扫描前应先清空旧数据，确保已删除的歌曲不会残留 |
| 输入 | 数据库中有旧歌曲数据，设备上部分歌曲已删除 |
| 预期输出 | 扫描后数据库只包含设备上现存的歌曲 |
| 测试类型 | 正常流程 |

---

### TC-035: getSongById - 获取指定歌曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-035 |
| 测试点描述 | getSongById 应返回指定 ID 的歌曲 |
| 输入 | songId=123 |
| 预期输出 | 返回对应的 Song 或 null（不存在时） |
| 测试类型 | 正常流程 |

---

### TC-036: searchSongs - 搜索歌曲

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-036 |
| 测试点描述 | searchSongs 应返回标题或艺术家匹配搜索词的歌曲 |
| 输入 | 歌曲列表 [Song1("Hello"), Song2("World"), Song3("Hello World")]，搜索 "hello" |
| 预期输出 | 返回 Song1 和 Song3（不区分大小写匹配标题或艺术家） |
| 测试类型 | 正常流程 |

---

### TC-037: searchSongs - 空搜索词

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-037 |
| 测试点描述 | 搜索词为空时应返回所有歌曲 |
| 输入 | 歌曲列表 [A,B,C,D]，搜索 "" |
| 预期输出 | 返回 [A,B,C,D] |
| 测试类型 | 边界条件 |

---

### TC-038: searchSongs - 无匹配结果

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-038 |
| 测试点描述 | 搜索词无匹配时应返回空列表 |
| 输入 | 歌曲列表 [A,B,C,D]，搜索 "xyz" |
| 预期输出 | 返回 [] |
| 测试类型 | 边界条件 |

---

### TC-039: 音频设备断开暂停

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-039 |
| 测试点描述 | 播放时音频设备（耳机/蓝牙）断开应自动暂停 |
| 输入 | isPlaying=true，音频设备断开（ACTION_AUDIO_BECOMING_NOISY 或 AudioDeviceCallback） |
| 预期输出 | isPlaying=false，wasPlayingBeforeDeviceRemoval=true |
| 测试类型 | 正常流程 |

---

### TC-040: 音频设备重连恢复播放

| 字段 | 值 |
|------|-----|
| 测试用例编号 | TC-040 |
| 测试点描述 | 音频设备重新连接且之前在播放应自动恢复播放 |
| 输入 | wasPlayingBeforeDeviceRemoval=true，音频设备重连 |
| 预期输出 | isPlaying=true，wasPlayingBeforeDeviceRemoval=false |
| 测试类型 | 正常流程 |

---

## 测试用例统计

| 测试类型 | 数量 |
|---------|------|
| 正常流程 | 25 |
| 边界条件 | 7 |
| 异常处理 | 8 |
| **总计** | 40 |
