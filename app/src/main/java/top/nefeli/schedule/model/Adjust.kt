package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * 调课记录实体类
 */
@Entity(
    tableName = "adjustments",
    foreignKeys = [ForeignKey(
        entity = Schedule::class,
        parentColumns = ["id"],
        childColumns = ["scheduleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("scheduleId")]
)
data class Adjust(
    val date: LocalDate,           // 日期
    val scheduleId: Long,          // ScheduleId
    val targetDate: LocalDate,     // 目标日期
    val startTime: LocalTime,      // 开始时间
    val endTime: LocalTime,        // 结束时间
    val originalPeriodId: Long?,   // 原作息节次
    val adjustType: String = "",   // 调整类型
    val note: String = ""          // 备注
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}