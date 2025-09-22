package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * 课表实体类
 */
@Entity(tableName = "timetables")
data class Timetable(
    val name: String,                    // 课表名称
    val semester: String = "",                // 学期
    val classId: String = "",           // 所属班级/用户ID
    val createdTime: LocalDateTime = LocalDateTime.now(), // 创建时间
    val note: String = ""               // 备注
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}