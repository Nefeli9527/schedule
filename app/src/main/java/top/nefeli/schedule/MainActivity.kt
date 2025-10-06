// app/src/main/java/top/nefeli/schedule/view/MainActivity.kt
package top.nefeli.schedule

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.data.SettingsRepository
import top.nefeli.schedule.util.CourseReminderManager
import top.nefeli.schedule.util.DoNotDisturbHelper
import top.nefeli.schedule.view.ScheduleApp
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory

var DEBUG = true

// Create a CompositionLocal to pass dynamic color state
val LocalDynamicColor = compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    private lateinit var repository: ScheduleRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var courseReminderManager: CourseReminderManager
    private lateinit var doNotDisturbHelper: DoNotDisturbHelper

    companion object {
        private const val TAG = "DEBUG"
    }

    // 请求通知权限
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限被授予，设置课程提醒
            courseReminderManager.scheduleAllCourseReminders(MainScope())
        } else {
            // 权限被拒绝
        }
    }

    // 请求勿扰权限
    private val requestDoNotDisturbPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // 用户从设置返回，检查是否获得了勿扰权限
        checkDoNotDisturbPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 处理通知点击事件
        handleNotificationIntent(intent)
        
        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上
//            window.setDecorFitsSystemWindows(false)
        } else {
            // Android 11以下
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }

        repository = ScheduleRepository(this)
        
        settingsRepository = SettingsRepository(this)
        
        val viewModelFactory = ScheduleViewModelFactory(repository, this)
        
        val settingsViewModelFactory = SettingsViewModelFactory(settingsRepository)
        
        courseReminderManager = CourseReminderManager(this, repository)
        
        doNotDisturbHelper = DoNotDisturbHelper(this)

        // 检查并请求通知权限
        checkAndRequestNotificationPermission()

        // 检查并请求勿扰权限（仅当设置中启用了该功能时）
        checkAndRequestDoNotDisturbPermissionIfNeeded()

        setContent {
            ScheduleAppWithTheme(viewModelFactory, settingsViewModelFactory, repository)
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到应用时检查勿扰权限状态
        checkAndRequestDoNotDisturbPermissionIfNeeded()
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 已经有权限，设置课程提醒
                    courseReminderManager.scheduleAllCourseReminders(MainScope())
                }

                else -> {
                    // 请求权限
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 13以下版本不需要运行时权限
            courseReminderManager.scheduleAllCourseReminders(MainScope())
        }
    }

    private fun checkAndRequestDoNotDisturbPermissionIfNeeded() {
        MainScope().launch {
            try {
                val settings = settingsRepository.loadSettings()
                // 只有在设置中启用了勿扰功能时才检查权限
                if (settings.doNotDisturbEnabled) {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (!notificationManager.isNotificationPolicyAccessGranted) {
                        // 显示提示信息
                        Toast.makeText(
                            this@MainActivity,
                            "需要勿扰权限以在上课时自动开启勿扰模式",
                            Toast.LENGTH_LONG
                        ).show()

                        // 请求勿扰权限
                        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        requestDoNotDisturbPermissionLauncher.launch(intent)
                    } else {
                        // 已获得勿扰权限
                    }
                } else {
                    // 勿扰功能未启用
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    private fun checkDoNotDisturbPermission() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Toast.makeText(this, "已获得勿扰权限", Toast.LENGTH_SHORT).show()
        } else {
            // 仍未获得勿扰权限
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // 处理通知点击事件
        intent?.let { handleNotificationIntent(it) }
    }

    /**
     * 处理通知点击事件
     */
    private fun handleNotificationIntent(intent: Intent) {
        when (intent.action) {
            "SHOW_COURSE_REMINDER" -> {
                // 显示课程提醒界面
                // 这里可以添加显示课程提醒界面的逻辑
            }

            "SHOW_COURSE_REMINDER_FULLSCREEN" -> {
                // 显示全屏课程提醒界面
                // 这里可以添加显示全屏课程提醒界面的逻辑
            }
        }
    }
    
    @Composable
    fun ScheduleAppWithTheme(
        viewModelFactory: ScheduleViewModelFactory,
        settingsViewModelFactory: SettingsViewModelFactory,
        repository: ScheduleRepository
    ) {
        val dynamicColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(LocalContext.current)
            true
        } else {
            false
        }
        
        CompositionLocalProvider(LocalDynamicColor provides dynamicColor) {
            MaterialTheme(
                colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicLightColorScheme(LocalContext.current)
                } else {
                    MaterialTheme.colorScheme
                }
            ) {
                ScheduleApp(viewModelFactory, settingsViewModelFactory, repository)
            }
        }
    }
}