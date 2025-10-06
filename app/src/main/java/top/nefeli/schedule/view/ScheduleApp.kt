package top.nefeli.schedule.view

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.DEBUG
import top.nefeli.schedule.LocalDynamicColor
import top.nefeli.schedule.R
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.view.screens.AddCourseScreen
import top.nefeli.schedule.view.screens.ImportScheduleScreen
import top.nefeli.schedule.view.screens.PeriodManagementScreen
import top.nefeli.schedule.view.screens.ScheduleScreen
import top.nefeli.schedule.view.screens.SettingsScreen
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory

@Composable
fun ScheduleApp(
    viewModelFactory: ScheduleViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    repository: ScheduleRepository = ScheduleRepository(LocalContext.current as ComponentActivity)
) {

    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val viewModel: ScheduleViewModel = viewModel(factory = viewModelFactory)
    val settings by settingsViewModel.settings.collectAsState()
    var currentScreen by remember { mutableStateOf("schedule") }
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }
    val backPressThreshold = 2000 // 2 seconds
    val useDynamicColor = LocalDynamicColor.current

    // 处理返回键逻辑
    val activity = (context as? ComponentActivity)
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentScreen == "schedule") {
                    // 在主页，检查是否启用双击退出
                    if (settings.doubleBackToExit) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime > backPressThreshold) {
                            Toast.makeText(context, R.string.exit_prompt, Toast.LENGTH_SHORT).show()
                            backPressedTime = currentTime
                        } else {
                            activity?.finish()
                        }
                    } else {
                        activity?.finish()
                    }
                } else {
                    // 不在主页，返回主页
                    currentScreen = "schedule"
                }
            }
        }
    }

    DisposableEffect(activity) {
        activity?.onBackPressedDispatcher?.addCallback(backCallback)
        onDispose {
            backCallback.remove()
        }
    }

    // 如果是调试模式，加载示例课表
    LaunchedEffect(DEBUG) {
        if (DEBUG) {
            val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val hasAddedSampleData = prefs.getBoolean("has_added_sample_data", false)
            
            // 只在第一次启动调试模式时添加示例数据
            if (!hasAddedSampleData) {
                repository.addSampleData()
                prefs.edit().putBoolean("has_added_sample_data", true).apply()
            }
        }
    }
    
    // 检查是否是首次启动并初始化默认数据
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        
        if (isFirstLaunch) {
            // 只有在非调试模式下才创建默认课表
            // 在调试模式下，addSampleData已经创建了默认课表
            if (!DEBUG) {
                viewModel.createDefaultTimetable()
            }
            
            // 标记已启动过
            prefs.edit().putBoolean("is_first_launch", false).apply()
        } else {
            // 检查是否有课表，如果没有则创建一个默认课表
            if (viewModel.timetables.value.isEmpty()) {
                viewModel.createDefaultTimetable()
            } else if (viewModel.currentTimetableId.value == null) {
                // 如果有课表但没有选中任何课表，则选中第一个
                viewModel.selectTimetable(viewModel.timetables.value.first().id)
            }
        }
    }

    // 使用 MaterialTheme 包装整个应用
    MaterialTheme(
        colorScheme = if (useDynamicColor) {
            MaterialTheme.colorScheme
        } else {
            MaterialTheme.colorScheme
        }
    ) {
        when (currentScreen) {
            "schedule" -> {
                var showCourseList by remember { mutableStateOf(false) }

                ScheduleScreen(
                    viewModel = viewModel,
                    settingsViewModel = settingsViewModel,
                    navigateToAddCourse = { currentScreen = "add_course" },
                    navigateToCourseList = { showCourseList = true },
                    navigateToSettings = { currentScreen = "settings" },
                    navigateToImport = { currentScreen = "import" },
                    onDebugModeChanged = { isEnabled ->
                        DEBUG = isEnabled
                        if (isEnabled) {
                            Toast.makeText(context, R.string.debug_mode_enabled, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                if (showCourseList) {
                    // TODO: 更新CourseListDialog以适配新的数据结构
                    showCourseList = false
                }
            }

            "add_course" -> AddCourseScreen(
                onBack = { currentScreen = "schedule" },
                onAddCourse = { course, schedules ->
                    viewModel.addCourseWithSchedules(course, schedules)
                    currentScreen = "schedule"
                },
                totalWeeks = settings.totalWeeks,
                timetableId = viewModel.currentTimetableId.value ?: -1 // 提供当前课表ID，-1表示无效
            )

            "settings" -> SettingsScreen(
                settingsViewModelFactory = settingsViewModelFactory,
                onBack = { currentScreen = "schedule" },
                navigateToTimetableSettings = { currentScreen = "timetable_settings" },
                navigateToPeriodManagement = { currentScreen = "period_management" }
            )

            "import" -> ImportScheduleScreen(
                scheduleViewModelFactory = viewModelFactory,
                settingsViewModelFactory = settingsViewModelFactory,
                onBack = { currentScreen = "schedule" },
                onImportComplete = { currentScreen = "schedule" }
            )

            "period_management" -> PeriodManagementScreen(
                scheduleViewModelFactory = viewModelFactory,
                settingsViewModelFactory = settingsViewModelFactory,
                onBack = { currentScreen = "schedule" }
            )
        }

        // 处理返回键逻辑 - 只有不在主页时才处理返回键
        if (currentScreen != "schedule") {
            BackHandler {
                currentScreen = "schedule" // 返回主页
            }
        }
    }
}