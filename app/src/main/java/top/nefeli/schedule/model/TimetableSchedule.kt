package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * 作息时间表实体类
 */
@Entity(tableName = "timetable_schedules")
data class TimetableSchedule(
    val name: String,              // 节次名称
    val startTime: LocalTime,      // 开始时间
    val endTime: LocalTime,        // 结束时间
    val periodType: String = "",   // 时段类型
    val sortOrder: Int = 0,        // 排序序号
    val note: String = ""          // 备注
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}