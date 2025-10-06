package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * 课程时间安排实体类
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = Course::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId"), Index("locationId"), Index("teacherId")]
)
@TypeConverters(Schedule.Converters::class)
data class Schedule(
    val courseId: Long,          // 关联课程
    val weeks: Set<Int>,         // 周次
    val dayOfWeek: Int,          // 周几 (1-7, 1表示周一)
    val startPeriod: Int,        // 开始节次
    val endPeriod: Int,          // 结束节次
    val locationId: Long,        // 地点Id
    val teacherId: Long          // 老师Id
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    class Converters {
        @TypeConverter
        fun fromWeeksSet(value: Set<Int>): String {
            return value.sorted().joinToString(",")
        }

        @TypeConverter
        fun toWeeksSet(value: String): Set<Int> {
            return if (value.isEmpty()) emptySet() else value.split(",").map { it.toInt() }.toSet()
        }
    }
}