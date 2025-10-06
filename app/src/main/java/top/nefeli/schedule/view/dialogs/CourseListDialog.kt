// app/src/main/java/top/nefeli/schedule/view/dialogs/CourseListDialog.kt
package top.nefeli.schedule.view.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.view.utils.getCourseColor
import top.nefeli.schedule.view.utils.isDarkColor

@Composable
fun CourseListDialog(
    courses: List<Course>, // 更新为新的数据类型
    onDismiss: () -> Unit,
    onDeleteCourse: (Long) -> Unit, // 更新为使用ID
    onEditCourse: (Course) -> Unit // 更新为新的数据类型
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "已添加的课程",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            if (courses.isEmpty()) {
                Text(
                    text = "暂无课程",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(courses) { course ->
                        // TODO: 需要更新以适配新的数据结构
                        // 这里需要获取课程的时间安排信息，但目前没有直接关联
                        val days = listOf("周一", "周二", "周三", "周四", "周五")
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditCourse(course) },
                            colors = CardDefaults.cardColors(containerColor = getCourseColor(course.name)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = course.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkColor(getCourseColor(course.name))) 
                                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "学分: ${course.credit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDarkColor(getCourseColor(course.name))) 
                                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "考试时间: ${course.examTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDarkColor(getCourseColor(course.name))) 
                                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    // TODO: 需要更新以适配新的数据结构
                                    // 需要从关联的Schedule表中获取时间和地点信息
                                    
                                    // 如果有备注，显示备注
                                    if (course.note.isNotEmpty()) {
                                        Text(
                                            text = "备注: ${course.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDarkColor(getCourseColor(course.name))) 
                                                       MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onDeleteCourse(course.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "删除课程",
                                        modifier = Modifier.rotate(45f),
                                        tint = if (isDarkColor(getCourseColor(course.name))) 
                                                   MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("关闭")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}