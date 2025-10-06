package top.nefeli.schedule.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import java.time.LocalDate
import java.time.LocalTime

class CourseReminderReceiver : BroadcastReceiver() {
    companion object;

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            "ACTION_DISMISS" -> {
                // 用户选择翘课
                handleDismiss(context)
            }

            "ACTION_REMIND_LATER" -> {
                // 用户选择稍后提醒
                handleRemindLater(context, intent)
            }

            "ACTION_REMIND_LATER_NOTIFICATION" -> {
                // 5分钟后再次提醒
                handleRemindLaterNotification(context, intent)
            }

            "ACTION_CONFIRM_CLASS" -> {
                // 用户选择好的，准备并触发进度通知
                handleConfirmClass(context, intent)
            }

            "TEST_REMINDER" -> {
                // 测试提醒
                val courseName = intent.getStringExtra("course_name") ?: "测试课程"
                val startPeriod = intent.getIntExtra("start_period", 1)
                val endPeriod = intent.getIntExtra("end_period", 1)

                // 重新构造时间段列表
                val periods = mutableListOf<LocalTime>()
                for (i in 0 until (endPeriod - startPeriod + 1) * 2) {
                    val timeStr = intent.getStringExtra("period_$i")
                    if (timeStr != null) {
                        try {
                            val time = LocalTime.parse(timeStr)
                            periods.add(time)
                        } catch (e: Exception) {
                            // 忽略解析错误
                        }
                    }
                }

                val dateStr = intent.getStringExtra("date") ?: LocalDate.now().toString()
                val date = try {
                    val parsedDate = LocalDate.parse(dateStr)
                    parsedDate
                } catch (e: Exception) {
                    LocalDate.now()
                }

                // 显示通知
                val notificationHelper = EnhancedNotificationHelper(context)
                notificationHelper.showCourseReminderNotification(
                    "测试提醒",
                    "$courseName 即将开始",
                    courseName,
                    startPeriod,
                    endPeriod,
                    periods,
                    date
                )
            }

            "COURSE_REMINDER" -> {
                // 应用启动时的课程提醒
                val courseName = intent.getStringExtra("course_name") ?: "未知课程"
                val startTime = intent.getStringExtra("start_time") ?: "未知时间"
                val startPeriod = intent.getIntExtra("start_period", 1)
                val endPeriod = intent.getIntExtra("end_period", 1)

                // 重新构造时间段列表
                val periods = mutableListOf<LocalTime>()
                for (i in 0 until (endPeriod - startPeriod + 1) * 2) {
                    val timeStr = intent.getStringExtra("period_$i")
                    if (timeStr != null) {
                        try {
                            val time = LocalTime.parse(timeStr)
                            periods.add(time)
                        } catch (e: Exception) {
                            // 忽略解析错误
                        }
                    }
                }

                val dateStr = intent.getStringExtra("date") ?: LocalDate.now().toString()
                val date = try {
                    val parsedDate = LocalDate.parse(dateStr)
                    parsedDate
                } catch (e: Exception) {
                    LocalDate.now()
                }

                // 显示通知
                val notificationHelper = EnhancedNotificationHelper(context)
                notificationHelper.showCourseReminderNotification(
                    "课程提醒",
                    "$courseName 将于 $startTime 开始",
                    courseName,
                    startPeriod,
                    endPeriod,
                    periods,
                    date
                )
            }

            else -> {
                // 默认处理方式（兼容之前的实现）
                val courseName = intent.getStringExtra("course_name") ?: "未知课程"
                val startPeriod = intent.getIntExtra("start_period", 1)
                val endPeriod = intent.getIntExtra("end_period", 1)

                // 重新构造时间段列表
                val periods = mutableListOf<LocalTime>()
                for (i in 0 until (endPeriod - startPeriod + 1) * 2) {
                    val timeStr = intent.getStringExtra("period_$i")
                    if (timeStr != null) {
                        try {
                            val time = LocalTime.parse(timeStr)
                            periods.add(time)
                        } catch (e: Exception) {
                            // 忽略解析错误
                        }
                    }
                }

                val dateStr = intent.getStringExtra("date") ?: LocalDate.now().toString()
                val date = try {
                    val parsedDate = LocalDate.parse(dateStr)
                    parsedDate
                } catch (e: Exception) {
                    LocalDate.now()
                }
                
                // 显示通知
                val notificationHelper = EnhancedNotificationHelper(context)
                notificationHelper.showCourseReminderNotification(
                    "课程提醒",
                    "$courseName 即将开始",
                    courseName,
                    startPeriod,
                    endPeriod,
                    periods,
                    date
                )
            }
        }
    }

    private fun handleDismiss(context: Context) {
        // 取消当前通知
        NotificationManagerCompat.from(context)
            .cancel(EnhancedNotificationHelper.NOTIFICATION_ID_COURSE_REMINDER)
    }

    private fun handleRemindLater(context: Context, originalIntent: Intent) {
        // 取消当前通知
        NotificationManagerCompat.from(context)
            .cancel(EnhancedNotificationHelper.NOTIFICATION_ID_COURSE_REMINDER)

        // 设置5分钟后的提醒
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reminderIntent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = "ACTION_REMIND_LATER_NOTIFICATION"
            // 复制原始意图中的所有额外数据
            originalIntent.extras?.let { extras ->
                putExtras(extras)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置5分钟后触发的闹钟
        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000 // 5分钟后
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
    }

    private fun handleRemindLaterNotification(context: Context, intent: Intent) {
        val courseName = intent.getStringExtra("course_name") ?: "未知课程"
        val startPeriod = intent.getIntExtra("start_period", 1)
        val endPeriod = intent.getIntExtra("end_period", 1)

        // 重新构造时间段列表
        val periods = mutableListOf<LocalTime>()
        for (i in 0 until (endPeriod - startPeriod + 1) * 2) {
            val timeStr = intent.getStringExtra("period_$i")
            if (timeStr != null) {
                try {
                    periods.add(LocalTime.parse(timeStr))
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        }

        val dateStr = intent.getStringExtra("date") ?: LocalDate.now().toString()
        val date = try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }

        // 显示通知
        val notificationHelper = EnhancedNotificationHelper(context)
        notificationHelper.showCourseReminderNotification(
            "课程提醒",
            "$courseName 即将开始",
            courseName,
            startPeriod,
            endPeriod,
            periods,
            date
        )
    }

    private fun handleConfirmClass(context: Context, originalIntent: Intent) {
        // 取消当前通知
        NotificationManagerCompat.from(context)
            .cancel(EnhancedNotificationHelper.NOTIFICATION_ID_COURSE_REMINDER)

        // 触发进度通知
        val courseName = originalIntent.getStringExtra("course_name") ?: "未知课程"
        val startPeriod = originalIntent.getIntExtra("start_period", 1)
        val endPeriod = originalIntent.getIntExtra("end_period", 1)

        // 构建并发送广播给CourseProgressReceiver
        val intent = Intent(context, CourseProgressReceiver::class.java).apply {
            putExtra("course_name", courseName)
            putExtra("start_period", startPeriod)
            putExtra("end_period", endPeriod)
            // 复制所有时间段信息
            for (i in 0 until (endPeriod - startPeriod + 1) * 2) {
                val timeStr = originalIntent.getStringExtra("period_$i")
                if (timeStr != null) {
                    putExtra("period_$i", timeStr)
                }
            }
            // 复制日期信息
            val dateStr = originalIntent.getStringExtra("date") ?: LocalDate.now().toString()
            putExtra("date", dateStr)
        }

        // 发送广播以触发进度通知
        context.sendBroadcast(intent)
    }
}