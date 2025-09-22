package top.nefeli.schedule.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.ScheduleViewModel
import top.nefeli.schedule.ScheduleViewModelFactory
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.ScheduleRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ScheduleRepository(this)
        val viewModelFactory = ScheduleViewModelFactory(repository)
        
        setContent {
            ScheduleApp(viewModelFactory)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleApp(
    viewModelFactory: ScheduleViewModelFactory
) {
    val viewModel: ScheduleViewModel = viewModel(factory = viewModelFactory)
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showCourseList by remember { mutableStateOf(false) }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = "课程表",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { showCourseList = true }) {
                            Icon(
                                Icons.Default.List, 
                                contentDescription = "课程列表",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddCourseDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加课程")
                    Text("添加课程")
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ScheduleTable(viewModel.scheduleData) { updatedData ->
                    // 数据更新现在由 ViewModel 处理
                }
                
                if (showAddCourseDialog) {
                    AddCourseDialog(
                        onDismiss = { showAddCourseDialog = false },
                        onAddCourse = { newCourse, period, day ->
                            viewModel.addCourse(period, day, newCourse)
                            showAddCourseDialog = false
                        }
                    )
                }
                
                if (showCourseList) {
                    CourseListDialog(
                        courses = viewModel.scheduleData,
                        onDismiss = { showCourseList = false },
                        onDeleteCourse = { period, day ->
                            viewModel.removeCourse(period, day)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleTable(scheduleData: Map<Pair<Int, Int>, Course>, onUpdateData: (Map<Pair<Int, Int>, Course>) -> Unit) {
    val daysOfWeek = listOf("节次", "周一", "周二", "周三", "周四", "周五")
    val periods = listOf("第1节", "第2节", "第3节", "第4节", "第5节")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 表格标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 表格内容
        periods.forEachIndexed { index, period ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 显示节次
                Text(
                    text = period,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // 显示每天的课程
                for (day in 1..5) {
                    val course = scheduleData[Pair(index + 1, day)]
                    Box(modifier = Modifier.weight(1f)) {
                        CourseCell(course)
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCell(course: Course?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (course != null) getCourseColor(course.name) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (course != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = course.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                   Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = course.teacher,
                        fontSize = 12.sp,
                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                   Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = course.classroom,
                        fontSize = 10.sp,
                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                   Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "空闲",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun getCourseColor(courseName: String): Color {
    return when (courseName) {
        "数学" -> Color(0xFFBB86FC) // 紫色
        "英语" -> Color(0xFF6200EE) // 深紫色
        "物理" -> Color(0xFF03DAC5) // 蓝绿色
        "化学" -> Color(0xFF018786) // 深蓝绿色
        "语文" -> Color(0xFFFFA500) // 橙色
        "体育" -> Color(0xFF4CAF50) // 绿色
        else -> Color(0xFFE0E0E0)  // 灰色
    }
}

fun getInitialScheduleData(): Map<Pair<Int, Int>, Course> {
    return emptyMap()
}

// 保存课程数据到 SharedPreferences
fun saveScheduleDataToPreferences(preferences: SharedPreferences, scheduleData: Map<Pair<Int, Int>, Course>) {
    val editor = preferences.edit()
    editor.clear() // 清除旧数据
    
    scheduleData.forEach { (time, course) ->
        val (period, day) = time
        val keyPrefix = "course_${period}_${day}"
        editor.putString("${keyPrefix}_name", course.name)
        editor.putString("${keyPrefix}_teacher", course.teacher)
        editor.putString("${keyPrefix}_classroom", course.classroom)
    }
    
    editor.apply()
}

// 从 SharedPreferences 加载课程数据
fun loadScheduleDataFromPreferences(preferences: SharedPreferences): Map<Pair<Int, Int>, Course> {
    val scheduleData = mutableMapOf<Pair<Int, Int>, Course>()
    
    val keys = preferences.all.keys
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
                
                courseEntries[entryKey]?.put(property, preferences.getString(key, "") ?: "")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onAddCourse: (Course, Int, Int) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var classroom by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(1) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "添加课程",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("教师") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = classroom,
                    onValueChange = { classroom = it },
                    label = { Text("教室") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "节次",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        FilterChip(
                            selected = selectedPeriod == i,
                            onClick = { selectedPeriod = i },
                            label = { Text("第${i}节") }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "星期",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val days = listOf("一", "二", "三", "四", "五")
                    for (i in 1..5) {
                        FilterChip(
                            selected = selectedDay == i,
                            onClick = { selectedDay = i },
                            label = { Text("周${days[i-1]}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (courseName.isNotBlank()) {
                        val course = Course(courseName, teacher, classroom)
                        onAddCourse(course, selectedPeriod, selectedDay)
                    }
                },
                enabled = courseName.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text("取消")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListDialog(
    courses: Map<Pair<Int, Int>, Course>,
    onDismiss: () -> Unit,
    onDeleteCourse: (Int, Int) -> Unit
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
                    items(courses.toList()) { item ->
                        val (time, course) = item
                        val (period, day) = time
                        val days = listOf("周一", "周二", "周三", "周四", "周五")
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
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
                                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                                   Color.White else Color.Black
                                    )
                                    Text(
                                        text = "教师: ${course.teacher}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                                   Color.White else Color.Black
                                    )
                                    Text(
                                        text = "地点: ${course.classroom}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                                   Color.White else Color.Black
                                    )
                                    Text(
                                        text = "时间: 周${days[day-1]} 第${period}节",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                                   Color.White else Color.Black
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteCourse(period, day) }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "删除课程",
                                        modifier = Modifier.rotate(45f),
                                        tint = if (course.name == "英语" || course.name == "物理" || course.name == "语文") 
                                                   Color.White else Color.Black
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
