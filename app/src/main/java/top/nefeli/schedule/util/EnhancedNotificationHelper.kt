package top.nefeli.schedule.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import top.nefeli.schedule.MainActivity
import top.nefeli.schedule.R
import java.time.LocalDate
import java.time.LocalTime

class EnhancedNotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID_COURSE_REMINDER = "course_reminder_channel"
        const val CHANNEL_ID_COURSE_PROGRESS = "course_progress_channel"

        const val NOTIFICATION_ID_COURSE_REMINDER = 1
        const val NOTIFICATION_ID_COURSE_PROGRESS = 2


    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 课程提醒通知渠道
        val courseReminderChannel = NotificationChannel(
            CHANNEL_ID_COURSE_REMINDER,
            "课程提醒",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "课程开始前的通知提醒"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(courseReminderChannel)

        // 课程进度通知渠道
        val courseProgressChannel = NotificationChannel(
            CHANNEL_ID_COURSE_PROGRESS,
            "课程进度",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "课程进行中的进度通知"
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(courseProgressChannel)
    }

    /**
     * 显示课程提醒通知
     */
    fun showCourseReminderNotification(
        title: String,
        content: String,
        courseName: String,
        startPeriod: Int,
        endPeriod: Int,
        periods: List<LocalTime>,
        date: LocalDate,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "SHOW_COURSE_REMINDER"
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 全屏意图，用于悬浮通知
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "SHOW_COURSE_REMINDER_FULLSCREEN"
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // "翘课" 按钮
        val dismissIntent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = "ACTION_DISMISS"
            putExtra("course_name", courseName)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            dismissIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // "稍后提醒" 按钮
        val remindLaterIntent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = "ACTION_REMIND_LATER"
            putExtra("course_name", courseName)
            putExtra("start_period", startPeriod)
            putExtra("end_period", endPeriod)
            // 使用从0开始的索引方式
            periods.forEachIndexed { index, time ->
                putExtra("period_$index", time.toString())
            }
            putExtra("date", date.toString())
        }
        val remindLaterPendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            remindLaterIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // "好的" 按钮
        val confirmIntent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = "ACTION_CONFIRM_CLASS"
            putExtra("course_name", courseName)
            putExtra("start_period", startPeriod)
            putExtra("end_period", endPeriod)
            // 使用从0开始的索引方式
            periods.forEachIndexed { index, time ->
                putExtra("period_$index", time.toString())
            }
            putExtra("date", date.toString())
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            confirmIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COURSE_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_launcher_foreground, "翘课", dismissPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "稍后提醒", remindLaterPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "好的", confirmPendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_COURSE_REMINDER, notification)
            } catch (e: Exception) {
                // 忽略错误
            }
        } else {
            // 忽略权限不足的情况
        }
    }

    /**
     * 显示课程进度通知
     */
    fun showCourseProgressNotification(
        courseName: String,
        status: String,
        timeInfo: String,
        maxProgress: Int,
        currentProgress: Int,
    ) {
        Log.d(
            "EnhancedNotificationHelper",
            "Showing course progress notification: course=$courseName, status=$status, timeInfo=$timeInfo, progress=$currentProgress/$maxProgress"
        )
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COURSE_PROGRESS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$courseName - $status")
            .setContentText(timeInfo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setProgress(maxProgress, currentProgress, false)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                Log.d(
                    "EnhancedNotificationHelper",
                    "Posting notification with ID: $NOTIFICATION_ID_COURSE_PROGRESS"
                )
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_COURSE_PROGRESS, notification)
            } catch (e: Exception) {
                Log.e("EnhancedNotificationHelper", "Error showing notification", e)
            }
        } else {
            Log.w("EnhancedNotificationHelper", "Notification permission not granted")
        }
    }

    /**
     * 显示通用通知
     */
    fun showGeneralNotification(
        title: String,
        content: String,
        autoCancel: Boolean = true,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_COURSE_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(autoCancel)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_COURSE_REMINDER, notification)
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 取消课程进度通知
     */
    fun cancelProgressNotification() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_COURSE_PROGRESS)
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 取消所有通知
     */
    fun cancelAllNotifications() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                NotificationManagerCompat.from(context).cancelAll()
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
}