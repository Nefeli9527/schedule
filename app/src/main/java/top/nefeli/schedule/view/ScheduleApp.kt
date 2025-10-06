package top.nefeli.schedule.view

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.launch
import top.nefeli.schedule.DEBUG
import top.nefeli.schedule.LocalDynamicColor
import top.nefeli.schedule.R
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.view.screens.AddCourseScreen
import top.nefeli.schedule.view.screens.ImportScheduleScreen
import top.nefeli.schedule.view.screens.PeriodManagementScreen
import top.nefeli.schedule.view.screens.ScheduleScreen
import top.nefeli.schedule.view.screens.SettingsScreen
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.time.LocalDate

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

        // 检查是否有即将开始的课程，如果有则显示课程进度通知
        checkAndShowUpcomingCourseNotification(context, viewModel)
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

/**
 * 检查是否有即将开始的课程，如果有则显示课程进度通知
 */
private fun checkAndShowUpcomingCourseNotification(context: Context, viewModel: ScheduleViewModel) {
    // 在后台线程中执行检查
    kotlinx.coroutines.MainScope().launch {
        try {
            Log.d("ScheduleApp", "Checking for upcoming courses...")

            // 等待一段时间确保数据加载完成
            kotlinx.coroutines.delay(1000)

            // 获取当前时间
            val now = java.time.LocalTime.now()
            val today = LocalDate.now()
            val dayOfWeek = today.dayOfWeek.value

            Log.d("ScheduleApp", "Current time: $now, today: $today, day of week: $dayOfWeek")

            // 获取当前课表的所有课程时间安排
            val schedules = viewModel.findAllScheldules()
            val periods = viewModel.period.value
            val courses = viewModel.courses.value

            Log.d(
                "ScheduleApp",
                "Found ${schedules.size} schedules, ${periods.size} periods, ${courses.size} courses"
            )
            
            // 如果没有时段数据，尝试重新加载
            if (periods.isEmpty()) {
                Log.d("ScheduleApp", "No periods found, trying to reload...")
                // 使用公共方法重新加载时段数据
                viewModel.initializeDefaultPeriods()
                kotlinx.coroutines.delay(500) // 等待加载完成

                // 重新获取时段数据
                val updatedPeriods = viewModel.period.value
                Log.d("ScheduleApp", "After reload: ${updatedPeriods.size} periods")

                // 如果重新加载后仍然没有数据，则直接返回
                if (updatedPeriods.isEmpty()) {
                    Log.d("ScheduleApp", "Still no periods after reload, exiting")
                    return@launch
                }
            }

            // 查找今天且即将开始的课程（在接下来15分钟内开始）
            for (schedule in schedules) {
                Log.d(
                    "ScheduleApp",
                    "Checking schedule: dayOfWeek=${schedule.dayOfWeek}, startPeriod=${schedule.startPeriod}"
                )
                
                // 检查是否是今天的课程
                if (schedule.dayOfWeek == dayOfWeek) {
                    Log.d("ScheduleApp", "Found today's schedule, checking course...")

                    // 获取课程信息
                    val course = courses.find { it.id == schedule.courseId }
                    if (course != null) {
                        Log.d("ScheduleApp", "Found course: ${course.name}")

                        // 获取课程开始时间
                        val period =
                            viewModel.period.value.find { it.id == schedule.startPeriod.toLong() }
                        if (period != null) {
                            Log.d("ScheduleApp", "Found period: startTime=${period.startTime}")

                            // 检查课程是否即将开始（在接下来15分钟内）
                            val classStartTime = period.startTime
                            val timeDiff =
                                java.time.Duration.between(now, classStartTime).toMinutes()
                            
                            Log.d("ScheduleApp", "Time difference: $timeDiff minutes")

                            // 如果课程在接下来15分钟内开始
                            if (timeDiff in 0..15) {
                                Log.d(
                                    "ScheduleApp",
                                    "Course is upcoming, sending reminder notification..."
                                )

                                // 发送课前提醒通知
                                sendCourseReminderNotification(
                                    context,
                                    course,
                                    schedule,
                                    viewModel.period.value,
                                    today
                                )
                                break // 只处理第一个匹配的课程
                            }
                            // 如果课程已经开始但在进行中（开始后90分钟内）
                            else if (timeDiff < 0 && timeDiff > -90) {
                                Log.d(
                                    "ScheduleApp",
                                    "Course is ongoing, sending reminder notification first..."
                                )

                                // 先发送课前提醒通知
                                sendCourseReminderNotification(
                                    context,
                                    course,
                                    schedule,
                                    viewModel.period.value,
                                    today
                                )
                                break // 只处理第一个匹配的课程
                            } else {
                                Log.d("ScheduleApp", "Course is not within time range")
                            }
                        } else {
                            Log.d(
                                "ScheduleApp",
                                "Period not found for startPeriod: ${schedule.startPeriod}"
                            )
                        }
                    } else {
                        Log.d("ScheduleApp", "Course not found for courseId: ${schedule.courseId}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ScheduleApp", "Error checking for upcoming courses", e)
        }
    }
}

/**
 * 发送课前提醒通知
 */
private fun sendCourseReminderNotification(
    context: Context,
    course: Course,
    schedule: Schedule,
    periods: List<Period>,
    date: LocalDate,
) {
    // 获取课程开始时间
    val period = periods.find { it.id == schedule.startPeriod.toLong() }
    val startTime = period?.startTime?.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        ?: "未知时间"

    // 创建提醒通知的Intent
    val intent =
        android.content.Intent(context, top.nefeli.schedule.util.CourseReminderReceiver::class.java)
            .apply {
                action = "COURSE_REMINDER" // 添加action
                putExtra("course_name", course.name)
                putExtra("start_time", startTime)
                putExtra("date", date.toString())
                putExtra("start_period", schedule.startPeriod)
                putExtra("end_period", schedule.endPeriod)
                // 添加时间段信息，使用从0开始的索引方式
                var index = 0
                for (periodNum in schedule.startPeriod..schedule.endPeriod) {
                    // 获取这节课的时段信息
                    val periodItem = periods.find { it.id == periodNum.toLong() }

                    if (periodItem != null) {
                        // 添加开始时间
                        putExtra("period_$index", periodItem.startTime.toString())
                        index++

                        // 添加结束时间
                        putExtra("period_$index", periodItem.endTime.toString())
                        index++
                    }
                }
            }

    // 发送广播以触发提醒通知
    context.sendBroadcast(intent)
    Log.d("ScheduleApp", "Reminder notification broadcast sent for course: ${course.name}")
}
