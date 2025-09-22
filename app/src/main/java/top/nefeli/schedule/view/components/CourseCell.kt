// app/src/main/java/top/nefeli/schedule/view/components/CourseCell.kt
package top.nefeli.schedule.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.view.utils.getCourseColor
import top.nefeli.schedule.view.utils.isDarkColor

@Composable
fun CourseCell(
    course: Course,
    schedule: Schedule?,
    teacher: Teacher?,
    location: Location?,
    rowSpan: Int = 1,
    rowHeight: Dp = 60.dp // 添加行高参数，默认值为60.dp
) {
    // 计算总高度
    val totalHeight = (rowHeight * rowSpan)

    // 对于没有课程的单元格，显示空白；对于有课程的单元格，显示Card
    if (course.name.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight), // 使用固定高度而不是最小高度
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = getCourseColor(course.name)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = course.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkColor(getCourseColor(course.name))) 
                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = if (rowSpan > 1) 3 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = teacher?.name ?: "未分配教师",
                        fontSize = 12.sp,
                        color = if (isDarkColor(getCourseColor(course.name))) 
                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = location?.let { "${it.building}${it.classroom}" } ?: "未分配教室",
                        fontSize = 12.sp,
                        color = if (isDarkColor(getCourseColor(course.name))) 
                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // 如果是连续课程，显示节次范围
                    if (schedule != null && schedule.startPeriod != schedule.endPeriod) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${schedule.startPeriod}-${schedule.endPeriod}",
                            fontSize = 12.sp,
                            color = if (isDarkColor(getCourseColor(course.name))) 
                                       MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    } else {
        // 没有课程时显示空白Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        )
    }
}