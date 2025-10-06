package top.nefeli.schedule.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Settings
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.util.Scanner

class ScheduleImporter {
    companion object {
        
        fun importScheduleFromFile(file: File): ImportResult {
            return try {
                val content = Scanner(FileInputStream(file), "UTF-8").useDelimiter("\\A").next()
                val result = parseScheduleFile(content)
                result
            } catch (e: Exception) {
                ImportResult.Error("导入失败: ${e.message}")
            }
        }
        
        private fun parseScheduleFile(content: String): ImportResult {
            try {
                val lines = content.lines()
                
                // 解析课程表设置
                val settingsJson = JsonParser.parseString(lines[2]) as JsonObject
                val startDateStr = settingsJson.get("startDate")?.asString
                val maxWeek = settingsJson.get("maxWeek")?.asInt
                val showSat = settingsJson.get("showSat")?.asBoolean
                val showSun = settingsJson.get("showSun")?.asBoolean
                
                // 解析课程信息
                val coursesJson = JsonParser.parseString(lines[4]) as JsonArray
                val courses = mutableListOf<WakeupCourse>()
                
                // 解析每一门课程
                coursesJson.forEach { element ->
                    val courseObj = element as JsonObject
                    val day = courseObj.get("day")?.asInt ?: 0
                    val room = courseObj.get("room")?.asString ?: ""
                    val startNode = courseObj.get("startNode")?.asInt ?: 0
                    val step = courseObj.get("step")?.asInt ?: 1
                    val startWeek = courseObj.get("startWeek")?.asInt ?: 1
                    val endWeek = courseObj.get("endWeek")?.asInt ?: 1
                    val teacher = courseObj.get("teacher")?.asString ?: ""
                    val id = courseObj.get("id")?.asInt ?: 0
                    
                    // 根据id找到对应的课程名称
                    val courseNamesJson = JsonParser.parseString(lines[3]) as JsonArray
                    var courseName = ""
                    courseNamesJson.forEach { nameElement ->
                        val nameObj = nameElement as JsonObject
                        if (nameObj.get("id")?.asInt == id) {
                            courseName = nameObj.get("courseName")?.asString ?: ""
                        }
                    }
                    
                    courses.add(
                        WakeupCourse(
                            courseName,
                            teacher,
                            room,
                            day,
                            startNode,
                            step,
                            startWeek,
                            endWeek
                        )
                    )
                }
                
                // 转换为我们的数据格式
                val settings = convertToSettings(startDateStr, maxWeek, showSat, showSun)
                val scheduleData = convertToScheduleData(courses)
                
                return ImportResult.Success(settings, scheduleData)
            } catch (e: Exception) {
                return ImportResult.Error("解析文件失败: ${e.message}")
            }
        }
        
        private fun convertToSettings(
            startDateStr: String?,
            maxWeek: Int?,
            showSat: Boolean?,
            showSun: Boolean?
        ): Settings {
            val startDate = try {
                if (startDateStr != null) {
                    // 处理日期格式 "2025-9-1"
                    val parts = startDateStr.split("-")
                    if (parts.size == 3) {
                        LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                    } else {
                        LocalDate.now().minusYears(1)
                    }
                } else {
                    LocalDate.now().minusYears(1)
                }
            } catch (e: Exception) {
                LocalDate.now().minusYears(1)
            }
            
            val totalWeeks = maxWeek ?: 22
            val showWeekends = (showSat == true) || (showSun == true)
            
            return Settings(
                numberOfPeriods = 9, // 默认值，可以根据需要调整
                showWeekends = showWeekends,
                semesterStartDate = startDate,
                totalWeeks = totalWeeks,
                enableWeekNavigation = true
            )
        }
        
        private fun convertToScheduleData(courses: List<WakeupCourse>): List<Course> {
            val scheduleData = mutableListOf<Course>()
            
            courses.forEach { wakeupCourse ->
                try {
                    // TODO: 需要更新以适配新的数据结构
                    // 这里需要创建Course对象，但缺少timetableId参数
                    val course = Course(
                        name = wakeupCourse.courseName,
                        type = "必修", // 默认值
                        credit = 0.0, // 默认值
                        examTime = "", // 默认值
                        note = "", // 默认值
                        timetableId = 0 // 需要提供有效的timetableId
                    )
                    
                    scheduleData.add(course)
                } catch (e: Exception) {
                    // 忽略转换错误
                }
            }
            
            return scheduleData
        }
    }
}

data class WakeupCourse(
    val courseName: String,
    val teacher: String,
    val room: String,
    val day: Int,
    val startNode: Int,
    val step: Int,
    val startWeek: Int,
    val endWeek: Int
)

sealed class ImportResult {
    data class Success(
        val settings: Settings,
        val scheduleData: List<Course> // 更新为新的数据类型
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}