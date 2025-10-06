package top.nefeli.schedule.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.nefeli.schedule.MainActivity
import top.nefeli.schedule.R
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import java.time.LocalDateTime

/**
 * 桌面小组件，用于显示最近的一节课
 */
class RecentCourseWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled called")
        // 当第一个小组件实例被创建时启动定期更新
        scheduleUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled called")
        // 当最后一个小组件实例被删除时取消定期更新
        cancelScheduledUpdate(context)
    }

    /**
     * 安排定期更新
     */
    private fun scheduleUpdate(context: Context) {
        Log.d(TAG, "scheduleUpdate called")
        // 这里可以设置定期更新，例如使用AlarmManager
        // 为了简化，我们现在只依赖系统默认的更新机制
    }

    /**
     * 取消定期更新
     */
    private fun cancelScheduledUpdate(context: Context) {
        Log.d(TAG, "cancelScheduledUpdate called")
        // 取消之前设置的定期更新
    }

    companion object {
        private const val TAG = "RecentCourseWidget"

        /**
         * 主动更新所有小组件实例
         */
        fun updateWidgets(context: Context) {
            Log.d("RecentCourseWidget", "updateWidgets called")
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, RecentCourseWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            if (appWidgetIds.isNotEmpty()) {
                Log.d("RecentCourseWidget", "Updating ${appWidgetIds.size} widget(s)")
                // 修复：直接调用updateAppWidget而不是onUpdate
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } else {
                Log.d("RecentCourseWidget", "No widgets to update")
            }
        }
        
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            Log.d(TAG, "updateAppWidget called for widget ID: $appWidgetId")

            val views = RemoteViews(context.packageName, R.layout.widget_recent_course)

            // 设置点击事件，点击小组件打开应用
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // 异步加载课程数据并更新界面
            val repository = ScheduleRepository(context)

            // 使用 CoroutineScope 启动协程
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val recentCourseInfo = getRecentCourseInfo(repository, context)
                    updateWidgetContent(views, recentCourseInfo, context)

                    // 通知 AppWidgetManager 更新小组件
                    withContext(Dispatchers.Main) {
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading recent course", e)
                    views.setTextViewText(R.id.course_name, context.getString(R.string.no_course))
                    views.setTextViewText(R.id.course_time, "")
                    views.setTextViewText(R.id.course_location, "")
                    views.setTextViewText(R.id.widget_info, "")
                    
                    // 添加错误信息到小组件（仅在调试模式下）
                    if (isDebugMode(context)) {
                        views.setTextViewText(R.id.widget_info, "错误: ${e.message}")
                        views.setViewVisibility(R.id.widget_info, android.view.View.VISIBLE)
                    }

                    // 通知 AppWidgetManager 更新小组件
                    withContext(Dispatchers.Main) {
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }

        /**
         * 检查是否处于调试模式
         */
        private fun isDebugMode(context: Context): Boolean {
            return context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
        }

        /**
         * 获取最近课程的信息
         *
         * 查找策略:
         * 1. 优先查找今天的课程
         * 2. 如果今天没有课程，则查找明天的课程
         * 3. 如果明天也没有课程，则查找本周剩余日期的第一门课程
         * 4. 在确定日期的课程中，优先显示：
         *    a. 尚未开始的课程
         *    b. 正在进行中的课程
         *    c. 当天的第一门课程（即使已结束）
         */
        private suspend fun getRecentCourseInfo(
            repository: ScheduleRepository,
            context: Context,
        ): RecentCourseInfo {
            Log.d(TAG, "getRecentCourseInfo called")

            // 获取所有课表
            val timetables = repository.getAllTimetables()
            if (timetables.isEmpty()) {
                Log.d(TAG, "No timetables found")
                return RecentCourseInfo()
            }

            // 使用第一个课表
            val timetable = timetables[0]
            Log.d(TAG, "Using timetable: ${timetable.name}")
            Log.d(TAG, "Timetable created time: ${timetable.createdTime}")

            // 获取当前周次
            val now = LocalDateTime.now()
            Log.d(TAG, "Current datetime: $now")

            // 获取当前课表的所有课程安排
            val schedules = repository.getSchedulesByTimetable(timetable.id)
            Log.d(TAG, "Found ${schedules.size} schedules in timetable ${timetable.id}")

            // 显示所有课程安排的详细信息用于调试
            for (schedule in schedules) {
                Log.d(
                    TAG,
                    "Schedule: ID=${schedule.id}, CourseID=${schedule.courseId}, Weeks=${schedule.weeks}, Day=${schedule.dayOfWeek}"
                )
            }

            // 计算当前周次 - 使用课表的创建日期作为参考
            val semesterStartDate = timetable.createdTime.toLocalDate()
            val daysBetween =
                java.time.temporal.ChronoUnit.DAYS.between(semesterStartDate, now.toLocalDate())
            val currentWeek = (daysBetween / 7 + 1).toInt()
            Log.d(TAG, "Semester start date: $semesterStartDate")
            Log.d(TAG, "Days between semester start and now: $daysBetween")
            Log.d(TAG, "Calculated current week: $currentWeek")
            
            // 过滤出本周的课程安排
            val currentWeekSchedules = schedules.filter { schedule ->
                schedule.weeks.contains(currentWeek)
            }
            Log.d(TAG, "Found ${currentWeekSchedules.size} schedules for current week $currentWeek")

            // 如果本周没有课程，尝试显示所有课程中最近的课程
            if (currentWeekSchedules.isEmpty()) {
                Log.d(TAG, "No schedules found for current week, showing all schedules")
            }

            // 获取今天的课程
            val today = now.dayOfWeek.value
            val todaySchedules = if (currentWeekSchedules.isNotEmpty()) {
                currentWeekSchedules.filter { it.dayOfWeek == today }.sortedBy { it.startPeriod }
            } else {
                schedules.filter { it.dayOfWeek == today }.sortedBy { it.startPeriod }
            }
            Log.d(TAG, "Found ${todaySchedules.size} schedules for today (day $today)")

            // 按优先级查找课程:
            // 1. 今天的课程
            if (todaySchedules.isNotEmpty()) {
                Log.d(TAG, "Processing today's courses")
                return findMostRelevantSchedule(todaySchedules, repository, now)
            }

            // 2. 明天的课程
            val tomorrow = if (today == 7) 1 else today + 1  // 周日(7)的下一天是周一(1)
            val tomorrowSchedules = if (currentWeekSchedules.isNotEmpty()) {
                currentWeekSchedules.filter { it.dayOfWeek == tomorrow }.sortedBy { it.startPeriod }
            } else {
                schedules.filter { it.dayOfWeek == tomorrow }.sortedBy { it.startPeriod }
            }
            Log.d(
                TAG,
                "Checking tomorrow's courses (day $tomorrow), found ${tomorrowSchedules.size} schedules"
            )

            if (tomorrowSchedules.isNotEmpty()) {
                Log.d(TAG, "Processing tomorrow's courses")
                return findMostRelevantSchedule(tomorrowSchedules, repository, now)
            }

            // 3. 本周剩余日期的第一门课程
            Log.d(TAG, "Checking for courses later this week")
            val nextAvailableSchedules = if (currentWeekSchedules.isNotEmpty()) {
                currentWeekSchedules
            } else {
                schedules
            }.filter { it.dayOfWeek > today } // 只查找本周剩余的日期
                .sortedWith(compareBy({ it.dayOfWeek }, { it.startPeriod }))
                .takeIf { it.isNotEmpty() }

            if (nextAvailableSchedules != null) {
                // 取最近一天的第一门课程
                val nextDay = nextAvailableSchedules.first().dayOfWeek
                val nextDaySchedules = nextAvailableSchedules.filter { it.dayOfWeek == nextDay }
                Log.d(TAG, "Found courses for later in the week (day $nextDay), processing them")
                return findMostRelevantSchedule(nextDaySchedules, repository, now)
            }

            // 4. 如果本周没有任何课程，尝试查找下周的课程
            Log.d(TAG, "Checking for courses next week")
            val nextWeekSchedules = schedules
                .filter { schedule ->
                    schedule.weeks.contains(currentWeek + 1)
                }
                .filter { it.dayOfWeek >= today } // 查找下周从今天开始的日期
                .sortedWith(compareBy({ it.dayOfWeek }, { it.startPeriod }))
                .takeIf { it.isNotEmpty() }

            if (nextWeekSchedules != null) {
                val nextDay = nextWeekSchedules.first().dayOfWeek
                val nextDaySchedules = nextWeekSchedules.filter { it.dayOfWeek == nextDay }
                Log.d(TAG, "Found courses for next week (day $nextDay), processing them")
                return findMostRelevantSchedule(nextDaySchedules, repository, now)
            }

            // 5. 最后的兜底方案：显示任意课程
            if (schedules.isNotEmpty()) {
                Log.d(TAG, "Showing any available schedule as fallback")
                val sortedSchedules = schedules.sortedBy { it.dayOfWeek }
                val firstDay = sortedSchedules.first().dayOfWeek
                val firstDaySchedules = sortedSchedules.filter { it.dayOfWeek == firstDay }
                return findMostRelevantSchedule(firstDaySchedules, repository, now)
            }

            Log.d(TAG, "No upcoming courses found")
            return RecentCourseInfo()
        }

        /**
         * 在给定的课程安排中查找最相关的课程
         *
         * 查找策略:
         * 1. 优先显示尚未开始的课程
         * 2. 如果没有未开始的课程，显示正在进行中的课程
         * 3. 如果没有正在进行的课程，显示最近结束的课程
         * 4. 如果所有课程都还没开始（比如在凌晨），则显示第一门课程
         */
        private suspend fun findMostRelevantSchedule(
            schedules: List<Schedule>,
            repository: ScheduleRepository,
            now: LocalDateTime,
        ): RecentCourseInfo {
            val currentTime = now.toLocalTime()
            Log.d(TAG, "Finding most relevant schedule, current time: $currentTime")

            // 按节次排序
            val sortedSchedules = schedules.sortedBy { it.startPeriod }

            // 1. 首先查找尚未开始的课程
            var recentSchedule: Schedule? = null
            for (schedule in sortedSchedules) {
                val period =
                    repository.getAllPeriods().find { it.sortOrder == schedule.startPeriod }
                if (period != null) {
                    // 检查课程是否尚未开始
                    val isAfter = period.startTime.isAfter(currentTime)
                    Log.d(
                        TAG,
                        "Checking upcoming schedule: ${period.startTime} > $currentTime = $isAfter"
                    )
                    if (isAfter) {
                        recentSchedule = schedule
                        Log.d(TAG, "Found upcoming schedule")
                        break
                    }
                }
            }

            // 2. 如果没有未开始的课程，查找正在进行中的课程
            if (recentSchedule == null) {
                for (schedule in sortedSchedules) {
                    val startPeriod =
                        repository.getAllPeriods().find { it.sortOrder == schedule.startPeriod }
                    val endPeriod =
                        repository.getAllPeriods().find { it.sortOrder == schedule.endPeriod }
                    if (startPeriod != null && endPeriod != null) {
                        // 检查课程是否正在进行中（已开始但未结束）
                        val isBefore =
                            startPeriod.startTime.isBefore(currentTime) || startPeriod.startTime.equals(
                                currentTime
                            )
                        val isAfter = endPeriod.endTime.isAfter(currentTime)
                        Log.d(
                            TAG,
                            "Checking ongoing schedule: ${startPeriod.startTime} <= $currentTime = $isBefore && ${endPeriod.endTime} > $currentTime = $isAfter"
                        )
                        if (isBefore && isAfter) {
                            recentSchedule = schedule
                            Log.d(TAG, "Found ongoing schedule")
                            break
                        }
                    }
                }
            }

            // 3. 如果没有正在进行的课程，查找最近结束的课程
            if (recentSchedule == null) {
                // 从后往前查找（从最后一节课开始），找到最近结束的课程
                for (i in sortedSchedules.size - 1 downTo 0) {
                    val schedule = sortedSchedules[i]
                    val endPeriod =
                        repository.getAllPeriods().find { it.sortOrder == schedule.endPeriod }
                    if (endPeriod != null) {
                        // 检查课程是否已经结束
                        val isBefore = endPeriod.endTime.isBefore(currentTime)
                        Log.d(
                            TAG,
                            "Checking finished schedule: ${endPeriod.endTime} < $currentTime = $isBefore"
                        )
                        if (isBefore) {
                            recentSchedule = schedule
                            Log.d(TAG, "Found recently finished schedule")
                            break
                        }
                    }
                }
            }

            // 4. 如果还是没有找到（可能所有课程都还没开始），则显示第一门课程
            if (recentSchedule == null && sortedSchedules.isNotEmpty()) {
                Log.d(TAG, "No specific schedule found, using first schedule")
                recentSchedule = sortedSchedules.first()
            }

            if (recentSchedule == null) {
                Log.d(TAG, "No recent schedule found")
                return RecentCourseInfo()
            }

            // 获取课程详细信息
            val course = repository.getCourseById(recentSchedule.courseId)
            val location = repository.getLocationById(recentSchedule.locationId)
            val teacher = repository.getTeacherById(recentSchedule.teacherId)
            val period =
                repository.getAllPeriods().find { it.sortOrder == recentSchedule.startPeriod }

            if (course == null) {
                Log.d(TAG, "Course not found for schedule ID: ${recentSchedule.courseId}")
                return RecentCourseInfo()
            }

            Log.d(TAG, "Found recent course: ${course.name}")

            return RecentCourseInfo(
                course = course,
                schedule = recentSchedule,
                location = location,
                teacher = teacher,
                period = period
            )
        }

        /**
         * 更新小组件内容
         */
        private fun updateWidgetContent(
            views: RemoteViews,
            recentCourseInfo: RecentCourseInfo,
            context: Context,
        ) {
            Log.d(TAG, "updateWidgetContent called")

            if (recentCourseInfo.course == null) {
                views.setTextViewText(R.id.course_name, context.getString(R.string.no_course))
                views.setTextViewText(R.id.course_time, "")
                views.setTextViewText(R.id.course_location, "")
                views.setTextViewText(R.id.widget_info, "")
                return
            }

            views.setTextViewText(R.id.course_name, recentCourseInfo.course.name)

            val timeText = buildString {
                val schedule = recentCourseInfo.schedule
                val period = recentCourseInfo.period
                if (schedule != null) {
                    append("周${getDayOfWeekString(schedule.dayOfWeek)} ")
                }
                if (period != null) {
                    append(
                        "${
                            period.startTime.toString().substring(0, 5)
                        }-${period.endTime.toString().substring(0, 5)}"
                    )
                }
            }
            views.setTextViewText(R.id.course_time, timeText)

            val locationText = buildString {
                val location = recentCourseInfo.location
                if (location != null) {
                    if (!location.campus.isNullOrEmpty()) {
                        append("${location.campus} ")
                    }
                    if (!location.building.isNullOrEmpty()) {
                        append("${location.building} ")
                    }
                    append(location.classroom)
                }
            }
            views.setTextViewText(R.id.course_location, locationText)

            // 显示小组件读取的信息（仅在调试模式下）
            if (isDebugMode(context)) {
                val infoText = buildString {
                    append("ID: ${recentCourseInfo.course.id}")
                    append(", 课表: ${recentCourseInfo.schedule?.courseId}")
                    if (recentCourseInfo.period != null) {
                        append(", 节次: ${recentCourseInfo.period.sortOrder}")
                    }
                }
                views.setTextViewText(R.id.widget_info, infoText)
                views.setViewVisibility(R.id.widget_info, android.view.View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_info, android.view.View.GONE)
            }
        }

        /**
         * 将数字星期转换为中文
         */
        private fun getDayOfWeekString(dayOfWeek: Int): String {
            return when (dayOfWeek) {
                1 -> "一"
                2 -> "二"
                3 -> "三"
                4 -> "四"
                5 -> "五"
                6 -> "六"
                7 -> "日"
                else -> ""
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive called with action: ${intent?.action}")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d(TAG, "onUpdate called for widget IDs: ${appWidgetIds.contentToString()}")

        // 更新所有小组件实例
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    /**
     * 用于存储最近课程信息的数据类
     */
    private data class RecentCourseInfo(
        val course: Course? = null,
        val schedule: Schedule? = null,
        val location: Location? = null,
        val teacher: Teacher? = null,
        val period: top.nefeli.schedule.model.Period? = null,
    )
}