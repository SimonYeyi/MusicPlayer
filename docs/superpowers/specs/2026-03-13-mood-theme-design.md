# 心情主题切换功能设计

## 1. 概述

提供 10 种心情主题供用户选择，切换主题后整个应用的颜色方案实时变化，主题选择通过 SharedPreferences 持久化。

## 2. 核心逻辑

### 2.1 数据模型

使用 `enum class` 定义 10 种心情主题，每种主题定义完整的 Material3 ColorScheme：

```kotlin
enum class MoodTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val onPrimaryColor: Color,
    val description: String
) {
    HAPPY("喜庆", Color(0xFFE53935), ...),        // 欢乐、庆祝、热情
    SAD("哀伤", Color(0xFF9E9E9E), ...),          // 忧郁、沉思、安静
    CALM("平静", Color(0xFF1E88E5), ...),         // 放松、安宁、和谐
    ENERGETIC("活力", Color(0xFFFF9800), ...),     // 充满活力、激情、运动
    ROMANTIC("浪漫", Color(0xFFE91E63), ...),      // 爱情、甜蜜、温柔
    MYSTERIOUS("神秘", Color(0xFF7B1FA2), ...),    // 深邃、未知、奇妙
    NATURAL("自然", Color(0xFF43A047), ...),        // 清新、生机、环保
    WARM("温暖", Color(0xFFFFC107), ...),           // 温馨、舒适、阳光
    COOL("冷酷", Color(0xFF37474F), ...),           // 冷静、理性、简约
    FRESH("清新", Color(0xFF00ACC1), ...),           // 清爽、干净、青春
}
```

### 2.2 主题持久化

使用 SharedPreferences 存储当前主题：

```kotlin
companion object {
    private const val PREFS_NAME = "music_player_prefs"
    private const val KEY_THEME = "current_theme"
}

private val savedTheme: MoodTheme
    get() {
        val themeName = sharedPreferences.getString(KEY_THEME, MoodTheme.CALM.name)
        return try {
            MoodTheme.valueOf(themeName ?: MoodTheme.CALM.name)
        } catch (e: IllegalArgumentException) {
            MoodTheme.CALM
        }
    }

fun setTheme(theme: MoodTheme) {
    sharedPreferences.edit().putString(KEY_THEME, theme.name).apply()
    _uiState.update { it.copy(currentTheme = theme) }
}
```

### 2.3 主题应用

主题通过 Compose `MaterialTheme` 动态应用：

```kotlin
MaterialTheme(
    colorScheme = ColorScheme(
        primary = currentTheme.primaryColor,
        secondary = currentTheme.secondaryColor,
        background = currentTheme.backgroundColor,
        surface = currentTheme.surfaceColor,
        onPrimary = currentTheme.onPrimaryColor,
        // ...
    )
)
```

## 3. 流程设计

### 3.1 主题切换流程

```
用户点击主题按钮 → 显示主题选择对话框 → 选择主题
  └── 更新 SharedPreferences → 更新 ViewModel 状态 → UI 重新渲染
```

### 3.2 应用启动恢复主题流程

```
应用启动 → 读取 SharedPreferences → 恢复上次主题 → 应用到 UI
```

## 4. UI 设计

### 4.1 主题切换按钮

```
┌─────────────────────────────────┐
│  [设置页面]                     │
│                                 │
│  🎨 当前主题：平静              │
│     [●]                        │
└─────────────────────────────────┘
```

点击后显示 `MoodThemePickerDialog`。

### 4.2 主题选择对话框（MoodThemePickerDialog）

```
┌─────────────────────────────────┐
│  选择心情主题                   │
├─────────────────────────────────┤
│  [●喜庆]  [●平静]              │
│  欢乐庆祝  放松安宁             │
│                                 │
│  [●活力]  [●浪漫]              │
│  激情运动  甜蜜温柔             │
│                                 │
│  [○哀伤]  [○神秘]              │
│  忧郁沉思  深邃奇妙             │
│                                 │
│  [○自然]  [○温暖]              │
│  清新生机  温馨舒适             │
│                                 │
│  [○冷酷]  [○清新]              │
│  冷静简约  清爽干净             │
├─────────────────────────────────┤
│           [取消]                │
└─────────────────────────────────┘
```

- 2 列网格布局
- 每个主题项：圆形颜色预览 + 主题名称 + 描述
- 选中项有边框高亮（animateColorAsState）
- 点击后立即应用并关闭对话框

## 5. 数据模型变更

### 5.1 新增文件

- `domain/model/MoodTheme.kt` - 主题枚举定义

### 5.2 ViewModel 层变更

```kotlin
// MusicUiState
val currentTheme: MoodTheme = MoodTheme.CALM
val showThemePicker: Boolean = false

// 方法
fun setTheme(theme: MoodTheme)
fun toggleThemePicker()
fun hideThemePicker()
```

### 5.3 UI 层变更

- 新增 `MoodThemePickerDialog` - 主题选择弹框
- 新增 `MoodThemeItem` - 单个主题卡片组件
- 新增 `ThemeSwitchButton` - 主题切换按钮

## 6. 修改文件清单

| 文件 | 变更类型 | 说明 |
|------|----------|------|
| `domain/model/MoodTheme.kt` | 新建 | 主题枚举定义 |
| `presentation/viewmodel/MusicViewModel.kt` | 修改 | 添加主题状态和方法 |
| `presentation/ui/components/MoodThemePicker.kt` | 新建 | 主题选择器 UI |
| `presentation/ui/MainActivity.kt` | 修改 | 应用主题到 MaterialTheme |
| `presentation/ui/screens/settings/SettingsScreen.kt` | 修改 | 添加主题切换入口 |

## 7. 成功/失败提示

- 切换主题：无 Toast，直接应用
- 异常恢复：读取 SharedPreferences 失败时默认使用 CALM（平静）主题
