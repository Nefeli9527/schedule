package top.nefeli.schedule.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.runBlocking
import top.nefeli.schedule.data.SettingsRepository
import java.time.LocalDate
import java.time.LocalTime

class CourseProgressReceiver : BroadcastReceiver() {
    companion object {
        private const val PROGRESS_UPDATE_INTERVAL: Long = 30000 // 5分钟更新一次，减少频率
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d("CourseProgressReceiver", "Received intent: ${intent.action}")
            
            val courseName = intent.getStringExtra("course_name") ?: "未知课程"
            val startPeriod = intent.getIntExtra("start_period", 1)
            val endPeriod = intent.getIntExtra("end_period", 1)

            Log.d(
                "CourseProgressReceiver",
                "Course: $courseName, startPeriod: $startPeriod, endPeriod: $endPeriod"
            )
            
            // 修复时间段解析逻辑
            val period: List<LocalTime> = (0 until (endPeriod - startPeriod + 1) * 2).map { i ->
                val timeStr = intent.getStringExtra("period_$i") ?: "00:00"
                LocalTime.parse(timeStr)
            }
            val date = LocalDate.parse(intent.getStringExtra("date") ?: LocalDate.now().toString())

            Log.d("CourseProgressReceiver", "Parsed period times: $period, date: $date")

            // 计算当前状态和进度信息
            val now = LocalTime.now()
            val (status, currentPeriod) = calculateCurrentStatus(
                now,
                period,
                startPeriod,
                endPeriod
            )
            val periodCount = endPeriod - startPeriod + 1

            Log.d(
                "CourseProgressReceiver",
                "Current status: $status, period: $currentPeriod, now: $now"
            )

            // 显示进度通知
            val notificationHelper = EnhancedNotificationHelper(context)

            // 计算时间信息
            val timeInfo = calculateTimeInfo(now, period, startPeriod, endPeriod, status)

            Log.d("CourseProgressReceiver", "Time info: $timeInfo")

            // 计算进度值（使用您的建议方法）
            val maxProgress = if (period.isNotEmpty()) {
                val startTime = period.first()
                val endTime = period.last()
                // 使用第一个和最后一个时间段之间的时间差作为最大进度值（以分钟为单位）
                java.time.Duration.between(startTime, endTime).toMinutes().toInt().coerceAtLeast(1)
            } else {
                100 // 默认值
            }

            // 使用第一个时间段和当前时间之间的时间差作为当前进度值
            val currentProgress = if (period.isNotEmpty()) {
                val startTime = period.first()
                // 使用第一个时间段和当前时间之间的时间差作为当前进度值（以分钟为单位）
                java.time.Duration.between(startTime, now).toMinutes().toInt()
                    .coerceIn(0, maxProgress)
            } else {
                0
            }

            Log.d("CourseProgressReceiver", "Progress: $currentProgress/$maxProgress")

            // 处理勿扰模式
            handleDoNotDisturbMode(context, status)

            when (status) {
                "下课" -> {
                    // 课程结束，显示最终通知然后取消
                    notificationHelper.showCourseProgressNotification(
                        courseName,
                        status,
                        timeInfo.ifEmpty { "课程已结束" },
                        maxProgress,
                        currentProgress
                    )
                    // 延迟一段时间后取消通知
                    Thread {
                        Thread.sleep(30000) // 30秒后取消通知
                        notificationHelper.cancelProgressNotification()
                    }.start()
                }

                else -> {
                    // 显示进度通知（持续性通知，不会因为点击而取消）
                    notificationHelper.showCourseProgressNotification(
                        courseName,
                        status,
                        timeInfo.ifEmpty { "第 $currentPeriod 节课" },
                        maxProgress,
                        currentProgress
                    )

                    // 设置定期更新通知，但仅对"上课中"和"课间"状态
                    if (status == "上课中" || status == "课间") {
                        scheduleProgressUpdate(context, intent)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CourseProgressReceiver", "Error in onReceive", e)
        }
    }

    /**
     * 安排进度更新
     */
    private fun scheduleProgressUpdate(context: Context, originalIntent: Intent) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                System.currentTimeMillis().toInt() % 10000, // 使用不同的requestCode避免冲突
                originalIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val nextUpdateTime = System.currentTimeMillis() + PROGRESS_UPDATE_INTERVAL

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextUpdateTime,
                pendingIntent
            )
        } catch (e: Exception) {
            // 忽略错误
        }
    }

    /**
     * 处理勿扰模式
     */
    private fun handleDoNotDisturbMode(context: Context, status: String) {
        // 检查设置中是否启用了勿扰功能
        val settingsRepository = SettingsRepository(context)
        val settings = runBlocking {
            try {
                settingsRepository.getSettings()
            } catch (e: Exception) {
                null
            }
        }

        // 只有在设置中启用了勿扰功能时才处理
        if (settings?.doNotDisturbEnabled == true) {
            val doNotDisturbHelper = DoNotDisturbHelper(context)

            // 检查是否已获得权限
            if (doNotDisturbHelper.isDoNotDisturbPermissionGranted()) {
                when (status) {
                    "上课中" -> {
                        // 上课时启用勿扰模式
                        doNotDisturbHelper.enableDoNotDisturb()
                    }

                    "下课" -> {
                        // 下课后禁用勿扰模式
                        doNotDisturbHelper.disableDoNotDisturb()
                    }
                    // 课间不改变勿扰模式状态
                }
            }
        }
    }

    /**
     * 计算当前状态（上课中/课间/下课等）和当前节次
     */
    private fun calculateCurrentStatus(
        now: LocalTime,
        periods: List<LocalTime>,
        startPeriod: Int,
        endPeriod: Int,
    ): Pair<String, Int> {
        try {
            // 根据当前时间判断是上课还是课间
            // 现在 periods 列表只包含当前课程的时间段（开始和结束时间交替）
            for (i in startPeriod..endPeriod) {
                val periodIndex = (i - startPeriod) * 2
                val classStartTime =
                    periods.getOrNull(periodIndex) ?: return Pair("未知", startPeriod)
                val classEndTime =
                    periods.getOrNull(periodIndex + 1) ?: return Pair("未知", startPeriod)

                // 判断当前是上课时间
                if (now in classStartTime..classEndTime) {
                    return Pair("上课中", i)
                }

                // 判断当前是课间时间（除了最后一节课后）
                if (i < endPeriod) {
                    val nextClassStartTime =
                        periods.getOrNull(periodIndex + 2) ?: return Pair("未知", startPeriod)
                    if (now in classEndTime..nextClassStartTime) {
                        return Pair("课间", i)
                    }
                }
            }

            // 如果在课程时间之外
            if (periods.isNotEmpty()) {
                val firstClassStart = periods.first()
                if (now.isBefore(firstClassStart)) {
                    return Pair("未开始", startPeriod)
                }

                val lastClassEnd = periods.last()
                if (now.isAfter(lastClassEnd)) {
                    return Pair("下课", startPeriod)
                }
            }
        } catch (e: Exception) {
            // 忽略错误
        }

        return Pair("上课中", startPeriod)
    }

    /**
     * 计算时间信息，包括剩余时间
     */
    private fun calculateTimeInfo(
        now: LocalTime,
        periods: List<LocalTime>,
        startPeriod: Int,
        endPeriod: Int,
        status: String,
    ): String {
        try {
            // 根据当前时间判断是上课还是课间
            // 现在 periods 列表只包含当前课程的时间段（开始和结束时间交替）
            for (i in startPeriod..endPeriod) {
                val periodIndex = (i - startPeriod) * 2
                val classStartTime = periods.getOrNull(periodIndex) ?: return "时间信息不可用"
                val classEndTime = periods.getOrNull(periodIndex + 1) ?: return "时间信息不可用"

                // 判断当前是上课时间
                if (now in classStartTime..classEndTime) {
                    val minutesLeft = java.time.Duration.between(now, classEndTime).toMinutes()
                    return "距离第${i}节课结束还有${minutesLeft}分钟"
                }

                // 判断当前是课间时间（除了最后一节课后）
                if (i < endPeriod) {
                    val nextClassStartTime =
                        periods.getOrNull(periodIndex + 2) ?: return "时间信息不可用"
                    if (now in classEndTime..nextClassStartTime) {
                        val minutesLeft =
                            java.time.Duration.between(now, nextClassStartTime).toMinutes()
                        return "距离第${i + 1}节课开始还有${minutesLeft}分钟"
                    }
                }
            }

            // 如果在课程时间之外
            if (periods.isNotEmpty()) {
                val firstClassStart = periods.first()
                if (now.isBefore(firstClassStart)) {
                    val minutesLeft = java.time.Duration.between(now, firstClassStart).toMinutes()
                    return "距离第${startPeriod}节课开始还有${minutesLeft}分钟"
                }

                val lastClassEnd = periods.last()
                if (now.isAfter(lastClassEnd)) {
                    return "课程已结束"
                }
            }
        } catch (e: Exception) {
            // 忽略错误
        }

        return "课程进行中"
    }
}