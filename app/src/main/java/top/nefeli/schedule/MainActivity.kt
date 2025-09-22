// app/src/main/java/top/nefeli/schedule/view/MainActivity.kt
package top.nefeli.schedule

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.data.SettingsRepository
import top.nefeli.schedule.view.ScheduleApp
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory

var DEBUG = true

// Create a CompositionLocal to pass dynamic color state
val LocalDynamicColor = compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        val repository = ScheduleRepository(this)
        val settingsRepository = SettingsRepository(this)
        val viewModelFactory = ScheduleViewModelFactory(repository, this)
        val settingsViewModelFactory = SettingsViewModelFactory(settingsRepository)

        setContent {
            ScheduleAppWithTheme(viewModelFactory, settingsViewModelFactory, repository)
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