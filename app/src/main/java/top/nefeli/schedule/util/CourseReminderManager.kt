package top.nefeli.schedule.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.view.utils.getWeekNumber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CourseReminderManager(
    private val context: Context,
    private val repository: ScheduleRepository,
) {
    companion object {
        private const val REMINDER_REQUEST_CODE_BASE = 1000

        // 假设学期开始日期为当年的9月1日，需要根据实际情况调整
        private val SEMESTER_START_DATE = LocalDate.of(LocalDate.now().year, 9, 1)
    }

    /**
     * 设置所有课程的提醒
     */
    fun scheduleAllCourseReminders(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val timetables = repository.getAllTimetables()
                if (timetables.isEmpty()) return@launch

                val currentTimetable = timetables[0] // 使用第一个课表
                val schedules = repository.getSchedulesByTimetable(currentTimetable.id)
                val periods = repository.getAllPeriods()
                val courses = repository.getCoursesByTimetable(currentTimetable.id)

                // 构建课程ID到课程对象的映射
                val courseMap = courses.associateBy { it.id }

                // 取消之前设置的所有提醒
                cancelAllReminders()

                // 为每个课程安排提醒
                schedules.forEach { schedule ->
                    // 获取课程信息
                    val course = courseMap[schedule.courseId]
                    if (course != null) {
                        scheduleCourseReminder(schedule, course, periods)
                    }
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 为单个课程设置提醒
     */
    private fun scheduleCourseReminder(schedule: Schedule, course: Course, periods: List<Period>) {
        val period = periods.find { it.id == schedule.startPeriod.toLong() }
        if (period == null) {
            return
        }

        // 为接下来的一周内每天设置提醒
        val today = LocalDate.now()
        var reminderCount = 0
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = date.dayOfWeek.value

            // 检查这天是否有课
            val currentWeekNumber = getWeekNumber(date, SEMESTER_START_DATE)
            if (schedule.dayOfWeek == dayOfWeek && schedule.weeks.contains(currentWeekNumber)) {
                // 计算提醒时间（课程开始前15分钟）
                val reminderTime = LocalDateTime.of(date, period.startTime).minusMinutes(15)

                // 设置提醒（包括过去的提醒，用于测试）
                setReminder(reminderTime, course, periods, schedule, date)
                reminderCount++
            }
        }
    }

    /**
     * 设置单个提醒
     */
    private fun setReminder(
        reminderTime: LocalDateTime,
        course: Course,
        periods: List<Period>,
        schedule: Schedule,
        date: LocalDate,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 获取课程开始时间
        val period = periods.find { it.id == schedule.startPeriod.toLong() }
        val startTime =
            period?.startTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "未知时间"

        // 直接传递课程信息而不是 Notification 对象
        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            putExtra("course_name", course.name)
            putExtra("start_period", schedule.startPeriod)
            putExtra("end_period", schedule.endPeriod)
            // 添加时间段信息
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
            putExtra("date", date.toString())
        }

        val requestCode =
            REMINDER_REQUEST_CODE_BASE + schedule.id.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmTime = reminderTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        // 为未来的提醒设置闹钟，也为不久之前的提醒设置闹钟（15分钟内）
        val now = LocalDateTime.now()
        if (reminderTime.isAfter(now) ||
            (reminderTime.isBefore(now) && reminderTime.isAfter(now.minusMinutes(15)))
        ) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }

    /**
     * 取消所有提醒
     */
    private fun cancelAllReminders() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 在实际应用中，你可能需要更精确地取消特定的提醒
        // 这里简化处理，只演示方法
    }

    /**
     * 发送测试通知，包含操作按钮
     */
    fun sendTestNotificationWithActions() {
        val notificationHelper = EnhancedNotificationHelper(context)
        // 创建测试时间段列表
        val periods = listOf(
            LocalTime.of(14, 0),   // 第1节开始
            LocalTime.of(14, 45),  // 第1节结束
            LocalTime.of(14, 55),  // 第2节开始
            LocalTime.of(15, 40)   // 第2节结束
        )

        notificationHelper.showCourseReminderNotification(
            "课程提醒测试",
            "下节课即将开始，请做好准备",
            "高等数学",
            1,  // startPeriod
            2,  // endPeriod
            periods,
            LocalDate.now()
        )
    }

    /**
     * 发送测试进度通知
     */
    fun sendTestProgressNotification() {
        val intent = Intent(context, CourseProgressReceiver::class.java).apply {
            putExtra("course_name", "高等数学")
            putExtra("start_period", 1)
            putExtra("end_period", 2)
            // 修正时间段索引，使其与实际逻辑匹配
            putExtra("period_0", "14:00")  // 第1节开始
            putExtra("period_1", "14:45")  // 第1节结束
            putExtra("period_2", "14:55")  // 第2节开始
            putExtra("period_3", "15:40")  // 第2节结束
            putExtra("date", LocalDate.now().toString())
        }

        // 发送广播以触发进度通知
        context.sendBroadcast(intent)
    }

    /**
     * 设置一分钟后的测试提醒
     * 用于快速测试通知功能
     */
    fun scheduleImmediateTestReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 设置提醒时间为当前时间的一分钟后
        val reminderTime = LocalDateTime.now().plusMinutes(1)
        val alarmTime = reminderTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        // 创建测试提醒的Intent
        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = "TEST_REMINDER"
            putExtra("course_name", "测试课程")
            putExtra("start_period", 1)
            putExtra("end_period", 2)
            // 添加测试时间段
            putExtra("period_0", "00:00")  // 第1节开始
            putExtra("period_1", "00:45")  // 第1节结束
            putExtra("period_2", "00:55")  // 第2节开始
            putExtra("period_3", "01:40")  // 第2节结束
            putExtra("date", LocalDate.now().toString())
        }

        val requestCode = REMINDER_REQUEST_CODE_BASE + 999
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )
    }
}