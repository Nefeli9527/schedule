package top.nefeli.schedule.util

import android.app.NotificationManager
import android.content.Context

class DoNotDisturbHelper(private val context: Context) {
    companion object;

    /**
     * 启用勿扰模式
     */
    fun enableDoNotDisturb() {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                // 启用勿扰模式，只允许重要通知
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                // 设置免打扰模式，隐藏所有通知
                // notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            } else {
                // 忽略权限不足
            }
        } catch (e: Exception) {
            // 忽略错误
        }
    }

    /**
     * 禁用勿扰模式
     */
    fun disableDoNotDisturb() {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                // 恢复正常模式
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            } else {
                // 忽略权限不足
            }
        } catch (e: Exception) {
            // 忽略错误
        }
    }

    /**
     * 检查是否已获得勿扰权限
     */
    fun isDoNotDisturbPermissionGranted(): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }
}