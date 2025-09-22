package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 地点实体类
 */
@Entity(tableName = "locations")
data class Location(
    val campus: String = "",     // 校区
    val building: String = "",   // 教学楼
    val classroom: String = ""   // 教室
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun toString(): String {
        return if(campus.isEmpty()) {
            if (building.isEmpty()) {
                classroom
            } else {
                if (classroom.isEmpty()) {
                    building
                }else {
                    "$campus $building $classroom"
                }
            }
        }else{
            if (building.isEmpty()) {
                "$campus $classroom"
            } else {
                if (classroom.isEmpty()) {
                    "$campus $building"
                }else {
                    "$campus $building $classroom"
                }
            }
        }
    }
}