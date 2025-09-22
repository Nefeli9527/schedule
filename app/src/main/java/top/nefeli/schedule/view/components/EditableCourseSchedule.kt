package top.nefeli.schedule.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule


data class CourseScheduleInfo(
    var courseId: Long,  // 添加
    var dayOfWeek: Int = 1,
    var startPeriod: Int = 1,
    var endPeriod: Int = 1,
    var weeks: Set<Int> = setOf(),
    var teacher: String = "",
    var location: Location = Location(),
    var teacherId: Long = -1,
    var locationId: Long = -1,
    var id: Long = -1,  // 添加ID字段来标识数据库中的记录
) {
    constructor(schedule: Schedule) : this(
        schedule.courseId,
        schedule.dayOfWeek,
        schedule.startPeriod,
        schedule.endPeriod,
        schedule.weeks,
        teacherId = schedule.teacherId,
        locationId = schedule.locationId,
        id = schedule.id
    )

    fun toSchedule(): Schedule {
        return Schedule(
            courseId = courseId,
            dayOfWeek = dayOfWeek,
            startPeriod = startPeriod,
            endPeriod = endPeriod,
            weeks = weeks,
            locationId = locationId,
            teacherId = teacherId
        ).apply {
            id = this@CourseScheduleInfo.id
        }
    }

    // 添加copy方法用于复制对象
    fun copy(): CourseScheduleInfo {
        val copied = CourseScheduleInfo(
            courseId = this.courseId,
            dayOfWeek = this.dayOfWeek,
            startPeriod = this.startPeriod,
            endPeriod = this.endPeriod,
            weeks = this.weeks.toSet(),
            teacher = this.teacher,
            location = Location(
                campus = this.location.campus,
                building = this.location.building,
                classroom = this.location.classroom
            ),
            teacherId = this.teacherId,
            locationId = this.locationId,
            id = this.id
        )
        return copied
    }
}

@Composable
fun EditableCourseSchedule(
    day: Int,
    period: Int,
    endPeriod: Int,
    weeks: Set<Int>,
    onEditWeeks: () -> Unit,
    onEditTime: () -> Unit,
    onEditTeacher: () -> Unit,
    onEditLocation: () -> Unit,
    teacher: String = "",
    location: String = "",
    onDelete: (() -> Unit)? = null,
    index: Int? = null,
    id: Long = -1,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (onDelete != null) MaterialTheme.colorScheme.secondaryContainer 
                           else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 如果是已添加的课程时间，显示序号和删除按钮
            if (onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "课程时间",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = "${stringResource(R.string.day_label)}${getDayLabel(day)} ${stringResource(R.string.period_format, period)}${if (endPeriod > period) "-${stringResource(R.string.period_format, endPeriod)}" else ""}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "周次: ${if (weeks.isNotEmpty()) "${weeks.minOf { it }}-${weeks.maxOf { it }}" else "未设置"}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "老师: ${teacher.ifEmpty { "未设置" }}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "地点: ${location.ifEmpty { "未设置" }}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "ID: ${id}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onEditWeeks,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("周次")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEditTime,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("时间")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEditTeacher,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("老师")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onEditLocation,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("地点")
                }
            }
        }
    }
}

fun getDayLabel(day: Int): String {
    val days = listOf("一", "二", "三", "四", "五", "六", "日")
    return if (day in 1..7) days[day - 1] else ""
}