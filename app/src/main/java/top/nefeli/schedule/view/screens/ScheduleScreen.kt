package top.nefeli.schedule.view.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import top.nefeli.schedule.R
import top.nefeli.schedule.view.components.ScheduleTable
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    settingsViewModel: SettingsViewModel,
    navigateToAddCourse: () -> Unit,
    navigateToCourseList: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToImport: () -> Unit,
    onDebugModeChanged: (Boolean) -> Unit
) {
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val teachers by viewModel.teachers.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val periods by viewModel.period.collectAsStateWithLifecycle()
    var tapCount by remember { mutableIntStateOf(0) }
    val tapTimeoutMs = 3000L // 3秒内点击有效
    val context = LocalContext.current
    
    LaunchedEffect(tapCount) {
        if (tapCount in 1..4) {
            // 如果在3秒内没有达到5次点击，则重置计数
            delay(tapTimeoutMs)
            tapCount = 0
        } else if (tapCount in 5..9) {
            // 5-9次点击用于测试进度通知
            delay(tapTimeoutMs)
            tapCount = 0
        }
    }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.schedule_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    actions = {
                        // 添加测试提醒按钮（仅在调试模式下显示）
                        if (top.nefeli.schedule.DEBUG) {
                            IconButton(onClick = {
                                // 发送一分钟后的测试提醒
                                val courseReminderManager =
                                    viewModel.getCourseReminderManager(context)
                                courseReminderManager.scheduleImmediateTestReminder()
                            }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "测试提醒",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        IconButton(onClick = navigateToImport) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.menu_import),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = navigateToCourseList) {
                            Icon(
                                Icons.Default.List, 
                                contentDescription = stringResource(R.string.menu_course_list),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = navigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.menu_settings),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = {
                            tapCount++
                            if (tapCount in 5..9) {
                                // 发送带操作按钮的测试通知，显示最近要上的课程
                                val courseReminderManager =
                                    viewModel.getCourseReminderManager(context)
                                courseReminderManager.scheduleImmediateTestReminder()
                            } else if (tapCount in 10..14) {
                                // 发送测试进度通知
                                val courseReminderManager =
                                    viewModel.getCourseReminderManager(context)
                                courseReminderManager.scheduleImmediateTestReminder()
                            } else if (tapCount >= 15) {
                                // 连续点击15次启用调试模式
                                onDebugModeChanged(true)
                                tapCount = 0
                            }
                        }
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = navigateToAddCourse,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_course_fab))
                    Text(stringResource(R.string.add_course_fab))
                }
            }
        ) { padding ->
            Box(modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())) {
                ScheduleTable(
                    courses = courses,
                    schedules = schedules,
                    teachers = teachers,
                    locations = locations,
                    settings = settings,
                    periods = periods // 传递作息时间表数据
                )
            }
        }
    }
}