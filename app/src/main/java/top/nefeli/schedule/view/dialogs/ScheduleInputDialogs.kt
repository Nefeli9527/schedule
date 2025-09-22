// app/src/main/java/top/nefeli/schedule/view/dialogs/ScheduleInputDialogs.kt
package top.nefeli.schedule.view.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.nefeli.schedule.view.components.WeekSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectionDialog(
    totalWeeks: Int,
    selectedWeeks: Set<Int>,
    onConfirm: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedWeeks by remember { mutableStateOf(selectedWeeks) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择周次") },
        text = {
            WeekSelector(
                totalWeeks = totalWeeks,
                selectedWeeks = currentSelectedWeeks,
                onWeeksChange = { currentSelectedWeeks = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(currentSelectedWeeks)
                    onDismiss()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectionDialog(
    selectedDay: Int,
    selectedPeriod: Int,
    endPeriod: Int,
    onConfirm: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDay by remember { mutableStateOf(selectedDay) }
    var currentPeriod by remember { mutableStateOf(selectedPeriod) }
    var currentEndPeriod by remember { mutableStateOf(endPeriod) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择课程时间") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "星期",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 星期选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = listOf("一", "二", "三", "四", "五", "六", "日")
                    val maxDay = 7
                    
                    for (i in 1..maxDay) {
                        FilterChip(
                            selected = currentDay == i,
                            onClick = { currentDay = i },
                            label = { Text("周${days[i-1]}") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "开始节次",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 节次选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..12) {
                        FilterChip(
                            selected = currentPeriod == i,
                            onClick = { 
                                currentPeriod = i
                                // 如果结束节次小于起始节次，则更新结束节次
                                if (currentEndPeriod < i) {
                                    currentEndPeriod = i
                                }
                            },
                            label = { Text("${i}节") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "结束节次",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 结束节次选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in currentPeriod..12) {
                        FilterChip(
                            selected = currentEndPeriod == i,
                            onClick = { currentEndPeriod = i },
                            label = { Text("${i}节") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(currentDay, currentPeriod, currentEndPeriod)
                    onDismiss()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherInputDialog(
    initialTeacher: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var teacher by remember { mutableStateOf(initialTeacher) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入老师姓名") },
        text = {
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("老师") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(teacher)
                    onDismiss()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInputDialog(
    initialLocation: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var location by remember { mutableStateOf(initialLocation) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("输入上课地点") },
        text = {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("上课地点") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(location)
                    onDismiss()
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}