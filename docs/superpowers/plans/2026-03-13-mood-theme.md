# 心情主题切换功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现 10 种心情主题切换功能，主题实时应用于整个应用，切换后通过 SharedPreferences 持久化。

**Architecture:**
- 领域层：`MoodTheme` 枚举（完整 ColorScheme 定义）
- ViewModel 层：`MusicViewModel`（主题状态和持久化）
- UI 层：`MoodThemePickerDialog` + `MoodThemePicker`（主题选择器）
- 持久化：SharedPreferences

**Tech Stack:** Jetpack Compose, Material Design 3, Hilt, SharedPreferences

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `domain/model/MoodTheme.kt` | 10 种心情主题枚举定义 |
| `presentation/viewmodel/MusicViewModel.kt` | 主题状态和切换逻辑 |
| `presentation/ui/components/MoodThemePicker.kt` | 主题选择器 UI 组件 |
| `presentation/ui/MainActivity.kt` | 应用主题到 MaterialTheme |
| `presentation/ui/screens/settings/SettingsScreen.kt` | 主题切换入口 |

---

## Task 1: 创建心情主题枚举

**Files:**
- Create: `domain/model/MoodTheme.kt`

- [ ] **Step 1: 创建 MoodTheme 枚举**

  ```kotlin
  enum class MoodTheme(
      val displayName: String,
      val primaryColor: Color,
      val secondaryColor: Color,
      val backgroundColor: Color,
      val surfaceColor: Color,
      val onPrimaryColor: Color,
      val onSecondaryColor: Color,
      val onBackgroundColor: Color,
      val onSurfaceColor: Color,
      val description: String
  ) {
      HAPPY("喜庆", Color(0xFFE53935), ...),        // 欢乐、庆祝、热情
      SAD("哀伤", Color(0xFF9E9E9E), ...),          // 忧郁、沉思、安静
      CALM("平静", Color(0xFF1E88E5), ...),         // 放松、安宁、和谐
      ENERGETIC("活力", Color(0xFFFF9800), ...),    // 充满活力、激情、运动
      ROMANTIC("浪漫", Color(0xFFE91E63), ...),     // 爱情、甜蜜、温柔
      MYSTERIOUS("神秘", Color(0xFF7B1FA2), ...),   // 深邃、未知、奇妙
      NATURAL("自然", Color(0xFF43A047), ...),       // 清新、生机、环保
      WARM("温暖", Color(0xFFFFC107), ...),          // 温馨、舒适、阳光
      COOL("冷酷", Color(0xFF37474F), ...),          // 冷静、理性、简约
      FRESH("清新", Color(0xFF00ACC1), ...),         // 清爽、干净、青春
  }

  fun getAllMoodThemes(): List<MoodTheme> = MoodTheme.entries.toList()
  ```

- [ ] **Commit**
  ```bash
  git add domain/model/MoodTheme.kt
  git commit -m "feat: 添加心情主题枚举定义"
  ```

---

## Task 2: ViewModel 添加主题状态和方法

**Files:**
- Modify: `presentation/viewmodel/MusicViewModel.kt`

- [ ] **Step 1: 在 MusicUiState 中添加主题状态**

  ```kotlin
  data class MusicUiState(
      val currentTheme: MoodTheme = MoodTheme.CALM,
      val showThemePicker: Boolean = false,
      // ...
  )
  ```

- [ ] **Step 2: 添加主题持久化逻辑**

  ```kotlin
  private val sharedPreferences: SharedPreferences =
      application.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)

  private val savedTheme: MoodTheme
      get() {
          val themeName = sharedPreferences.getString("current_theme", MoodTheme.CALM.name)
          return try {
              MoodTheme.valueOf(themeName ?: MoodTheme.CALM.name)
          } catch (e: IllegalArgumentException) {
              MoodTheme.CALM
          }
      }

  init {
      // 恢复保存的主题
      _uiState.update { it.copy(currentTheme = savedTheme) }
  }
  ```

- [ ] **Step 3: 添加主题切换方法**

  ```kotlin
  fun setTheme(theme: MoodTheme) {
      sharedPreferences.edit().putString("current_theme", theme.name).apply()
      _uiState.update { it.copy(currentTheme = theme) }
  }

  fun toggleThemePicker() {
      _uiState.update { it.copy(showThemePicker = true) }
  }

  fun hideThemePicker() {
      _uiState.update { it.copy(showThemePicker = false) }
  }
  ```

- [ ] **Commit**
  ```bash
  git add presentation/viewmodel/MusicViewModel.kt
  git commit -m "feat: MusicViewModel 添加心情主题状态和切换逻辑"
  ```

---

## Task 3: 创建主题选择器 UI

**Files:**
- Create: `presentation/ui/components/MoodThemePicker.kt`

- [ ] **Step 1: 创建 MoodThemePickerDialog**

  - 2 列网格布局
  - 每个主题项显示：颜色圆点 + 名称 + 描述
  - 选中项边框动画（animateColorAsState）
  - 点击后调用 `setTheme()` 并关闭

  ```kotlin
  @Composable
  fun MoodThemePickerDialog(
      currentTheme: MoodTheme,
      onThemeSelected: (MoodTheme) -> Unit,
      onDismiss: () -> Unit
  )
  ```

- [ ] **Step 2: 创建 MoodThemeItem**

  ```kotlin
  @Composable
  fun MoodThemeItem(
      theme: MoodTheme,
      isSelected: Boolean,
      onClick: () -> Unit,
      modifier: Modifier = Modifier
  )
  ```

- [ ] **Commit**
  ```bash
  git add presentation/ui/components/MoodThemePicker.kt
  git commit -m "feat: 添加心情主题选择器 UI"
  ```

---

## Task 4: 应用主题到 MaterialTheme

**Files:**
- Modify: `presentation/ui/MainActivity.kt`

- [ ] **Step 1: 在 MainActivity 中应用主题**

  ```kotlin
  val currentTheme = viewModel.uiState.collectAsStateWithLifecycle().value.currentTheme

  MaterialTheme(
      colorScheme = ColorScheme(
          primary = currentTheme.primaryColor,
          secondary = currentTheme.secondaryColor,
          background = currentTheme.backgroundColor,
          surface = currentTheme.surfaceColor,
          onPrimary = currentTheme.onPrimaryColor,
          onSecondary = currentTheme.onSecondaryColor,
          onBackground = currentTheme.onBackgroundColor,
          onSurface = currentTheme.onSurfaceColor,
      )
  ) {
      // App content
  }
  ```

- [ ] **Step 2: 在 SettingsScreen 中添加主题切换入口**

  ```kotlin
  // 设置页面中添加主题选择按钮，点击后显示 MoodThemePickerDialog
  if (uiState.showThemePicker) {
      MoodThemePickerDialog(
          currentTheme = uiState.currentTheme,
          onThemeSelected = { viewModel.setTheme(it) },
          onDismiss = { viewModel.hideThemePicker() }
      )
  }
  ```

- [ ] **Commit**
  ```bash
  git add presentation/ui/MainActivity.kt presentation/ui/screens/settings/SettingsScreen.kt
  git commit -m "feat: 应用心情主题到 MaterialTheme"
  ```

---

## Task 5: 构建与测试

**Files:**
- None

- [ ] **Step 1: 构建验证**

  ```bash
  ./gradlew assembleDebug
  ```

- [ ] **Step 2: 功能测试**

  - [ ] 应用启动恢复上次主题
  - [ ] 切换主题后整个应用颜色实时变化
  - [ ] 10 种主题全部可选择
  - [ ] 切换主题后退出应用再进入，主题保持
  - [ ] 异常情况：SharedPreferences 无数据时默认 CALM 主题

- [ ] **Commit**
  ```bash
  git add .
  git commit -m "feat: 完成心情主题切换功能"
  ```
