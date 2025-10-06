// app/src/main/java/top/nefeli/schedule/view/utils/Utils.kt
package top.nefeli.schedule.view.utils

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// 预设的颜色列表，使用 Material You 动态颜色
private val courseColors = listOf(
    Color(android.graphics.Color.parseColor("#FF6200EE")), // 系统主色调
    Color(android.graphics.Color.parseColor("#FF03DAC5")), // 系统辅助色调
    Color(android.graphics.Color.parseColor("#FFBB86FC")), // 系统第三色调
    Color(android.graphics.Color.parseColor("#FF018786")), // 系统强调色调
    Color(android.graphics.Color.parseColor("#FFFFA500")), // 橙色
    Color(android.graphics.Color.parseColor("#FF4CAF50")), // 绿色
    Color(android.graphics.Color.parseColor("#FF2196F3")), // 蓝色
    Color(android.graphics.Color.parseColor("#FFF44336")), // 红色
    Color(android.graphics.Color.parseColor("#FFFF9800")), // 深橙色
    Color(android.graphics.Color.parseColor("#FF9C27B0")), // 紫色
    Color(android.graphics.Color.parseColor("#FF795548")), // 棕色
    Color(android.graphics.Color.parseColor("#FF607D8B"))  // 蓝灰色
)

// 用于存储课程名称和颜色的映射关系
private val courseColorMap = mutableMapOf<String, Color>()

fun getCourseColor(courseName: String): Color {
    // 空课程名使用灰色
    if (courseName.isEmpty()) {
        return Color(android.graphics.Color.parseColor("#FFD3D3D3")) // 使用系统灰色
    }
    
    // 如果已经为该课程分配过颜色，则直接返回
    if (courseColorMap.containsKey(courseName)) {
        return courseColorMap[courseName]!!
    }
    
    // 根据课程名称的哈希值选择颜色
    val hash = courseName.hashCode()
    val colorIndex = kotlin.math.abs(hash) % courseColors.size
    val color = courseColors[colorIndex]
    
    // 将课程名称和颜色的映射关系保存起来
    courseColorMap[courseName] = color
    
    return color
}

// 判断颜色是否为深色的辅助函数
fun isDarkColor(color: Color): Boolean {
    // 使用相对亮度公式判断颜色深浅
    val brightness = (color.red * 299 + color.green * 587 + color.blue * 114) / 1000
    return brightness < 0.5
}

/**
 * 根据学期开始日期计算周数
 * @param date 要计算的日期
 * @param semesterStartDate 学期开始日期
 * @return 周数（从1开始）
 */
fun getWeekNumber(date: LocalDate, semesterStartDate: LocalDate): Int {
    // 计算两个日期之间相隔的周数
    val weeksBetween = ChronoUnit.WEEKS.between(semesterStartDate, date)
    // 周数从1开始计算
    return (weeksBetween + 1).toInt()
}