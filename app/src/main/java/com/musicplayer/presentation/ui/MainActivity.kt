package com.musicplayer.presentation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.media.RingtoneManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musicplayer.presentation.navigation.MusicNavHost
import com.musicplayer.presentation.navigation.Screen
import com.musicplayer.presentation.ui.components.MiniPlayer
import com.musicplayer.presentation.ui.components.MoodThemePickerDialog
import com.musicplayer.presentation.viewmodel.MusicViewModel
import com.musicplayer.util.RingtoneHelper
import com.musicplayer.util.ShareHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var currentViewModel: MusicViewModel? = null

    companion object {
        private const val PREFS_NAME = "music_player_prefs"
        private const val KEY_MUSIC_PERMISSION_REQUESTED = "music_permission_requested"
        private const val KEY_NOTIFICATION_PERMISSION_REQUESTED = "notification_permission_requested"
        private const val KEY_RETURNED_FROM_SETTINGS = "returned_from_settings"
    }

    // 标记用户是否刚从设置页面返回
    private var returnedFromSettings = false

    // 音乐权限Launcher
    private val requestMusicPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 标记已经请求过
        sharedPreferences.edit().putBoolean(KEY_MUSIC_PERMISSION_REQUESTED, true).apply()

        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // 音乐权限已授予，继续检查通知权限
            checkAndRequestNotificationPermission()
        } else {
            // 音乐权限被拒绝，退出app，下次打开继续申请
            finish()
        }
    }

    // 通知权限Launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 标记已经请求过
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, true).apply()

        if (isGranted) {
            // 通知权限已授予
            currentViewModel?.onPermissionGranted()
        } else {
            // 通知权限被拒绝，退出app，下次打开继续申请
            finish()
        }
    }

    // 删除文件系统弹框Launcher
    private var pendingDeleteSongId: Long = -1L
    private val deleteFileLauncher: ActivityResultLauncher<IntentSenderRequest> = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            currentViewModel?.onDeleteFileConfirmed(pendingDeleteSongId)
        } else {
            currentViewModel?.onDeleteFileDismissed()
        }
        pendingDeleteSongId = -1L
    }

    // 铃声选择器Launcher
    private var pendingRingtoneType: RingtoneHelper.RingtoneType? = null
    private val ringtonePickerLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val type = pendingRingtoneType
        pendingRingtoneType = null
        if (result.resultCode == RESULT_OK && type != null) {
            val pickedUri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val success = RingtoneHelper.setRingtone(this, type, pickedUri)
            currentViewModel?.onRingtonePickerResult(success)
        } else {
            currentViewModel?.onRingtonePickerResult(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边缘到边缘显示
        enableEdgeToEdge()
        window.statusBarColor = Color.Black.copy(alpha = 0.02f).toArgb()
        window.navigationBarColor = Color.Black.copy(alpha = 0.02f).toArgb()

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            val viewModel: MusicViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val pendingDeleteRequest by viewModel.pendingDeleteRequest.collectAsStateWithLifecycle()

            // 监听铃声设置结果 Toast
            LaunchedEffect(Unit) {
                viewModel.ringtoneToastMessage.collectLatest { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            // 监听分享事件
            LaunchedEffect(Unit) {
                viewModel.shareSongEvent.collectLatest { song ->
                    if (!ShareHelper.shareSong(this@MainActivity, song)) {
                        Toast.makeText(this@MainActivity, "分享失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // 监听铃声选择器请求
            LaunchedEffect(Unit) {
                viewModel.ringtonePickerRequest.collectLatest { request ->
                    pendingRingtoneType = request.type
                    val intent = RingtoneHelper.getRingtonePickerIntent(request.type, request.songUri)
                    ringtonePickerLauncher.launch(intent)
                }
            }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // 创建动态主题
            val colorScheme = remember(uiState.currentTheme) {
                val theme = uiState.currentTheme
                ColorScheme(
                    primary = theme.primaryColor,
                    onPrimary = theme.onPrimaryColor,
                    primaryContainer = theme.secondaryColor,
                    onPrimaryContainer = theme.onPrimaryColor,
                    inversePrimary = theme.primaryColor,
                    secondary = theme.secondaryColor,
                    onSecondary = theme.onPrimaryColor,
                    secondaryContainer = theme.surfaceColor,
                    onSecondaryContainer = theme.primaryColor,
                    tertiary = theme.primaryColor,
                    onTertiary = theme.onPrimaryColor,
                    tertiaryContainer = theme.surfaceColor,
                    onTertiaryContainer = theme.primaryColor,
                    background = theme.backgroundColor,
                    onBackground = theme.primaryColor,
                    surface = theme.surfaceColor,
                    onSurface = theme.primaryColor,
                    surfaceVariant = theme.surfaceColor,
                    onSurfaceVariant = theme.primaryColor,
                    surfaceTint = theme.primaryColor,
                    inverseSurface = theme.primaryColor,
                    inverseOnSurface = theme.onPrimaryColor,
                    error = Color(0xFFB00020),
                    onError = Color.White,
                    errorContainer = Color(0xFFFCD8DF),
                    onErrorContainer = Color(0xFF8C0009),
                    outline = theme.secondaryColor,
                    outlineVariant = theme.surfaceColor,
                    scrim = Color(0xFF000000),
                    surfaceBright = theme.surfaceColor,
                    surfaceDim = theme.surfaceColor,
                    surfaceContainer = theme.surfaceColor,
                    surfaceContainerHigh = theme.surfaceColor,
                    surfaceContainerHighest = theme.surfaceColor,
                    surfaceContainerLow = theme.surfaceColor,
                    surfaceContainerLowest = theme.surfaceColor
                )
            }

            // 处理删除文件请求
            LaunchedEffect(pendingDeleteRequest) {
                pendingDeleteRequest?.let { request ->
                    try {
                        val deleteRequest = MediaStore.createDeleteRequest(
                            contentResolver,
                            listOf(request.songUri)
                        )
                        pendingDeleteSongId = request.songId
                        val intentSenderRequest = IntentSenderRequest.Builder(deleteRequest.intentSender).build()
                        deleteFileLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        viewModel.onDeleteFileDismissed()
                    }
                }
            }

            MaterialTheme(colorScheme = colorScheme) {
                // 检查并请求权限（只在首次Launch时检查）
                var permissionsChecked by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (!permissionsChecked) {
                        permissionsChecked = true
                        currentViewModel = viewModel
                        checkAndRequestPermissions()
                    }
                }

                val showMiniPlayer = uiState.playbackState.currentSong != null

                // 权限被拒绝弹框（Material3 样式）
                if (uiState.showPermissionDeniedDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text(uiState.permissionDeniedTitle) },
                        text = { Text(uiState.permissionDeniedMessage) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.dismissPermissionDeniedDialog()
                                    openAppSettings()
                                }
                            ) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    viewModel.dismissPermissionDeniedDialog()
                                    finish()
                                }
                            ) {
                                Text("退出")
                            }
                        }
                    )
                }

                // 铃声类型选择对话框
                if (viewModel.showRingtoneTypeDialog.collectAsState().value) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onDismissRingtoneDialog() },
                        title = { Text("设为铃声") },
                        text = {
                            Column {
                                RingtoneHelper.RingtoneType.entries.forEach { type ->
                                    ListItem(
                                        headlineContent = { Text(type.title) },
                                        modifier = Modifier.clickable {
                                            viewModel.onRingtoneTypeSelected(type)
                                        }
                                    )
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { viewModel.onDismissRingtoneDialog() }) {
                                Text("取消")
                            }
                        }
                    )
                }

                // 铃声权限解释对话框
                if (viewModel.showRingtonePermissionDialog.collectAsState().value) {
                    AlertDialog(
                        onDismissRequest = { viewModel.onDismissRingtoneDialog() },
                        title = { Text("提示") },
                        text = { Text("设置铃声需要特殊权限，请在设置中授权后重试。") },
                        confirmButton = {
                            TextButton(onClick = { viewModel.onGoToSettingsForRingtone() }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.onDismissRingtoneDialog() }) {
                                Text("取消")
                            }
                        }
                    )
                }

                // 心情主题选择对话框
                if (uiState.showThemePicker) {
                    MoodThemePickerDialog(
                        currentTheme = uiState.currentTheme,
                        onThemeSelected = { theme -> viewModel.setTheme(theme) },
                        onDismiss = { viewModel.hideThemePicker() }
                    )
                }

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showMiniPlayer && uiState.playbackState.currentSong != null && currentRoute != "play_detail") {
                            MiniPlayer(
                                song = uiState.playbackState.currentSong!!,
                                isPlaying = uiState.playbackState.isPlaying,
                                currentPosition = uiState.playbackState.currentPosition,
                                duration = uiState.playbackState.duration,
                                onPlayPauseClick = { viewModel.togglePlayPause() },
                                onNextClick = { viewModel.playNext() },
                                onDetailClick = { navController.navigate("play_detail") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                ) { paddingValues ->
                    MusicNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        showMiniPlayer = showMiniPlayer,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val musicPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 检查音乐权限
        val allMusicGranted = musicPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allMusicGranted) {
            // 检查是否已经请求过权限
            val permissionRequested = sharedPreferences.getBoolean(KEY_MUSIC_PERMISSION_REQUESTED, false)

            if (permissionRequested) {
                // 已经请求过，检查是否永久拒绝
                val shouldShowRationale = musicPermissions.any {
                    shouldShowRequestPermissionRationale(it)
                }
                if (!shouldShowRationale) {
                    // 永久拒绝，显示提示框
                    currentViewModel?.onPermissionDenied()
                    showPermissionDeniedDialog(
                        "音乐权限",
                        "需要授予存储和通知权限才能完整使用应用，请在设置中开启权限"
                    )
                    return
                }
            }
            // 请求音乐权限
            requestMusicPermissionLauncher.launch(musicPermissions)
            return
        }

        // 音乐权限已授予，检查通知权限
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!notificationGranted) {
                // 检查是否已经请求过权限
                val permissionRequested = sharedPreferences.getBoolean(KEY_NOTIFICATION_PERMISSION_REQUESTED, false)

                if (permissionRequested) {
                    // 已经请求过，检查是否永久拒绝
                    val shouldShowRationale = shouldShowRequestPermissionRationale(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (!shouldShowRationale) {
                        // 永久拒绝，显示提示框
                        currentViewModel?.onPermissionDenied()
                        showPermissionDeniedDialog(
                            "通知权限",
                            "需要授予存储和通知权限才能完整使用应用，请在设置中开启权限"
                        )
                        return
                    }
                }
                // 请求通知权限
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // 所有权限都已授予
        currentViewModel?.onPermissionGranted()
    }

    private fun showPermissionDeniedDialog(permissionName: String, message: String) {
        currentViewModel?.showPermissionDeniedDialog("$permissionName 被拒绝", message)
    }

    private fun openAppSettings() {
        returnedFromSettings = true
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // 检查是否刚从设置页面返回
        if (returnedFromSettings) {
            returnedFromSettings = false
            checkPermissionsGranted()
        }
        // 检查是否刚从铃声设置页面返回
        val pendingRingtone = currentViewModel?.pendingRingtoneSongId?.value
        if (pendingRingtone != null) {
            currentViewModel?.onReturnFromSettingsForRingtone()
        }
    }

    private fun checkPermissionsGranted() {
        val musicPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allMusicGranted = musicPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allMusicGranted) {
            // 音乐权限未授予，显示提示框让用户选择
            currentViewModel?.onPermissionDenied()
            showPermissionDeniedDialog(
                "音乐权限",
                "需要授予存储和通知权限才能完整使用应用，请在设置中开启权限"
            )
            return
        }

        // 音乐权限已授予，检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!notificationGranted) {
                // 通知权限未授予，继续申请
                checkAndRequestNotificationPermission()
                return
            }
        }

        // 所有权限都已授予，通知ViewModel
        currentViewModel?.onPermissionGranted()
    }
}
