package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 设置实体类
 */
@Entity(tableName = "settings")
data class Settings(
    val numberOfPeriods: Int = 9,              // 节数设置
    val showWeekends: Boolean = false,         // 是否显示周末
    val semesterStartDate: LocalDate = LocalDate.now(), // 学期开始日期
    val totalWeeks: Int = 22,                  // 总周数
    val enableWeekNavigation: Boolean = true,  // 是否启用周次导航
    val doubleBackToExit: Boolean = true,      // 双击返回退出
    val doNotDisturbEnabled: Boolean = false,  // 上课勿扰功能开关
    val notificationEnabled: Boolean = true,   // 通知开关
    val theme: String = "auto",                 // 主题设置
) {
    @PrimaryKey
    var id: Long = 1
}