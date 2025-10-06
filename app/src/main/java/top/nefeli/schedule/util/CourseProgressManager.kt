package top.nefeli.schedule.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.LocalTime

class CourseProgressManager(private val context: Context) {
    companion object {
        private const val TAG = "CourseProgressManager"
        private const val REMINDER_REQUEST_CODE_BASE = 2000

        // 假设学期开始日期为当年的9月1日，需要根据实际情况调整
        private val SEMESTER_START_DATE = LocalDate.of(LocalDate.now().year, 9, 1)
    }

    /**
     * 为课程设置进度提醒
     * @param courseName 课程名称
     * @param startPeriod 开始节次
     * @param endPeriod 结束节次
     * @param periods 节次时间列表
     * @param date 课程日期
     */
    fun scheduleCourseProgress(
        courseName: String,
        startPeriod: Int,
        endPeriod: Int,
        periods: List<LocalTime>,
        date: LocalDate,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CourseProgressReceiver::class.java).apply {
            putExtra("course_name", courseName)
            putExtra("start_period", startPeriod)
            putExtra("end_period", endPeriod)
            // 保存所有节次的时间信息，修正索引
            periods.forEachIndexed { index, time ->
                val periodIndex = startPeriod * 2 + index
                putExtra("period_$periodIndex", time.toString())
            }
            putExtra("date", date.toString())
        }

        val requestCode = REMINDER_REQUEST_CODE_BASE + (System.currentTimeMillis() % 1000).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置立即触发的进度提醒
        val alarmTime = System.currentTimeMillis() + 1000 // 1秒后触发

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTime,
            pendingIntent
        )
    }
}