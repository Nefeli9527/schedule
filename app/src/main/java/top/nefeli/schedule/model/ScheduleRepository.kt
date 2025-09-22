package top.nefeli.schedule.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

data class Course(
    val name: String,
    val teacher: String,
    val classroom: String
)

class ScheduleRepository(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
    
    fun loadSchedule(): Map<Pair<Int, Int>, Course> {
        val scheduleData = mutableMapOf<Pair<Int, Int>, Course>()
        
        val keys = sharedPreferences.all.keys
        val courseEntries = mutableMapOf<String, MutableMap<String, String>>()
        
        // 将键值对组织成课程条目
        keys.forEach { key ->
            if (key.startsWith("course_")) {
                val parts = key.split("_")
                if (parts.size >= 4) {
                    val entryKey = "${parts[1]}_${parts[2]}" // period_day
                    val property = parts[3] // name, teacher, classroom
                    
                    if (!courseEntries.containsKey(entryKey)) {
                        courseEntries[entryKey] = mutableMapOf()
                    }
                    
                    courseEntries[entryKey]?.put(property, sharedPreferences.getString(key, "") ?: "")
                }
            }
        }
        
        // 从组织好的数据创建 Course 对象
        courseEntries.forEach { (entryKey, properties) ->
            val keyParts = entryKey.split("_")
            if (keyParts.size == 2) {
                val period = keyParts[0].toIntOrNull() ?: 0
                val day = keyParts[1].toIntOrNull() ?: 0
                
                if (period > 0 && day > 0) {
                    val name = properties["name"] ?: ""
                    val teacher = properties["teacher"] ?: ""
                    val classroom = properties["classroom"] ?: ""
                    
                    if (name.isNotEmpty()) {
                        scheduleData[Pair(period, day)] = Course(name, teacher, classroom)
                    }
                }
            }
        }
        
        return scheduleData
    }
    
    fun saveSchedule(scheduleData: Map<Pair<Int, Int>, Course>) {
        sharedPreferences.edit {
            clear() // 清除旧数据
            
            scheduleData.forEach { (time, course) ->
                val (period, day) = time
                val keyPrefix = "course_${period}_${day}"
                putString("${keyPrefix}_name", course.name)
                putString("${keyPrefix}_teacher", course.teacher)
                putString("${keyPrefix}_classroom", course.classroom)
            }
        }
    }
    
    fun addCourse(period: Int, day: Int, course: Course): Map<Pair<Int, Int>, Course> {
        val currentSchedule = loadSchedule().toMutableMap()
        currentSchedule[Pair(period, day)] = course
        saveSchedule(currentSchedule)
        return currentSchedule
    }
    
    fun removeCourse(period: Int, day: Int): Map<Pair<Int, Int>, Course> {
        val currentSchedule = loadSchedule().toMutableMap()
        currentSchedule.remove(Pair(period, day))
        saveSchedule(currentSchedule)
        return currentSchedule
    }
}
