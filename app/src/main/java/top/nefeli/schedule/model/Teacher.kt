package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 老师实体类
 */
@Entity(tableName = "teachers")
data class Teacher(
    val name: String             // 老师姓名
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}