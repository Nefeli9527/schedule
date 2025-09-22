package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * 课程实体类
 */
@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = Timetable::class,
        parentColumns = ["id"],
        childColumns = ["timetableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("timetableId")]
)
@TypeConverters(Course.Converters::class)
data class Course(
    val name: String,           // 课程名称
    val type: String,           // 课程类型
    val credit: Double,         // 学分
    val examTime: String = "",  // 考试时间
    val note: String = "",      // 备注
    val timetableId: Long       // 关联到具体课表
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    class Converters {
        @TypeConverter
        fun fromCredit(value: Double): String {
            return value.toString()
        }

        @TypeConverter
        fun toCredit(value: String): Double {
            return value.toDoubleOrNull() ?: 0.0
        }
    }
}