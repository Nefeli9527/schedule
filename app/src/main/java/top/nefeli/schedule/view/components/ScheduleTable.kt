package top.nefeli.schedule.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Settings
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.TimetableSchedule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleTable(
    courses: List<Course>,
    schedules: List<Schedule>,
    teachers: List<Teacher>,
    locations: List<Location>,
    settings: Settings,
    timetableSchedules: List<TimetableSchedule> = emptyList(), // 添加作息时间表参数
) {
    val daysOfWeek = buildList {
        add(stringResource(R.string.period_label))
        if (settings.showWeekends) {
            add(stringResource(R.string.monday))
            add(stringResource(R.string.tuesday))
            add(stringResource(R.string.wednesday))
            add(stringResource(R.string.thursday))
            add(stringResource(R.string.friday))
            add(stringResource(R.string.saturday))
            add(stringResource(R.string.sunday))
        } else {
            add(stringResource(R.string.monday))
            add(stringResource(R.string.tuesday))
            add(stringResource(R.string.wednesday))
            add(stringResource(R.string.thursday))
            add(stringResource(R.string.friday))
        }
    }

    val numberOfDays = if (settings.showWeekends) 7 else 5
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val estimatedHeaderHeight = 120.dp
    val availableHeight = screenHeight - estimatedHeaderHeight - 32.dp
    val minHeightForFiveRows = availableHeight / 6
    val calculatedRowHeight = availableHeight / settings.numberOfPeriods
    val rowHeight = when {
        calculatedRowHeight < 70.dp -> 70.dp
        calculatedRowHeight < minHeightForFiveRows -> minHeightForFiveRows
        else -> calculatedRowHeight
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        val currentDate = LocalDate.now()
        
        val daysBetween = ChronoUnit.DAYS.between(settings.semesterStartDate, currentDate)
        val calculatedWeekNumber = if (daysBetween >= 0) (daysBetween / 7).toInt() + 1 else 1
        val currentWeekNumber = when {
            calculatedWeekNumber < 1 -> 1
            calculatedWeekNumber > settings.totalWeeks -> settings.totalWeeks
            else -> calculatedWeekNumber
        }

        val selectedWeek by remember { mutableIntStateOf(currentWeekNumber) }
        var showDebugDialog by remember { mutableStateOf(false) }
        var showDateSelectionDialog by remember { mutableStateOf(false) }
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }
        var showDatePicker by remember { mutableStateOf(false) }
        var actualSelectedWeek by remember { mutableIntStateOf(selectedWeek) }

        val displaySchedule = remember(courses, schedules, teachers, locations, selectedWeek, actualSelectedWeek) {

            
            val result = createDisplaySchedule(
                courses,
                schedules,
                teachers,
                locations,
                settings.numberOfPeriods,
                numberOfDays,
                actualSelectedWeek
            )
            
            result
        }
        
        if (showDebugDialog) {
            AlertDialog(
                onDismissRequest = { showDebugDialog = false },
                title = { Text(stringResource(R.string.debug_info)) },
                text = {
                    Column {
                        Text("开学日期: ${settings.semesterStartDate}")
                        Text("当前日期: $currentDate")
                        Text("天数差: $daysBetween")
                        Text("计算周数: $calculatedWeekNumber")
                        Text("当前周数: $currentWeekNumber")
                        Text("总周数: ${settings.totalWeeks}")
                        Text("SelectedWeek: $selectedWeek")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDebugDialog = false }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
        
        if (showDateSelectionDialog) {
            AlertDialog(
                onDismissRequest = { showDateSelectionDialog = false },
                title = { Text("选择调课日期") },
                text = {
                    Column {
                        Text("当前选择日期: ${selectedDate.monthValue}月${selectedDate.dayOfMonth}日")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.select))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
//                        val daysFromStart = ChronoUnit.DAYS.between(settings.semesterStartDate, selectedDate)
//                        val weekNumber = if (daysFromStart >= 0) (daysFromStart / 7).toInt() + 1 else 1

                        showDateSelectionDialog = false
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateSelectionDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
        
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            
            LaunchedEffect(showDatePicker) {
                if (showDatePicker) {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        selectedDate = newDate
                    }
                }
            }
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { 
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            selectedDate = newDate
                        }
                        showDatePicker = false 
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

//        if(DEBUG) {
//            Box(modifier = Modifier.fillMaxWidth()) {
//                IconButton(
//                    onClick = { showDebugDialog = true },
//                    modifier = Modifier.align(Alignment.TopEnd)
//                ) {
//                    Text(stringResource(R.string.debug_info), color = MaterialTheme.colorScheme.primary)
//                }
//            }
//        }
        
        var userSelectedWeek by remember { mutableStateOf<Int?>(null) }
        actualSelectedWeek = userSelectedWeek ?: selectedWeek


        if (settings.enableWeekNavigation) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        val newWeek = (userSelectedWeek ?: selectedWeek) - 1
                        userSelectedWeek = newWeek
//                        if (userSelectedWeek == null) {
//                            selectedWeek = newWeek
//                        }
                    },
                    enabled = (userSelectedWeek ?: selectedWeek) > 1
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.previous_week)
                    )
                }

                Text(
                    text = stringResource(R.string.week_number, actualSelectedWeek),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        val newWeek = (userSelectedWeek ?: selectedWeek) + 1
                        userSelectedWeek = newWeek
//                        if (userSelectedWeek == null) {
//                            selectedWeek = newWeek
//                        }
                    },
                    enabled = (userSelectedWeek ?: selectedWeek) < settings.totalWeeks
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.next_week)
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.week_number, selectedWeek),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 获取作息时间表数据
        // val timetableSchedules by remember { mutableStateOf<List<TimetableSchedule>>(emptyList()) }
        // 注意：这里应该通过 ViewModel 或 Repository 获取实际的作息时间表数据
        // 目前我们使用空列表，后续需要通过依赖注入获取真实数据

        // 先恢复原来的布局确保功能正常
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = stringResource(R.string.period_label),
                modifier = Modifier
                    .weight(0.5f)
                    .padding(8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            for (i in 1..numberOfDays) {
                val dayOfWeek = if (settings.showWeekends) i else if (i >= 6) i + 1 else i
                val dayDate = settings.semesterStartDate.plusDays(((actualSelectedWeek) - 1) * numberOfDays + (dayOfWeek - 1).toLong())

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = true,
                            onClick = {
                                selectedDate = dayDate
                                val daysFromStart = ChronoUnit.DAYS.between(
                                    settings.semesterStartDate,
                                    selectedDate
                                )
                                val weekNumber =
                                    if (daysFromStart >= 0) (daysFromStart / 7).toInt() + 1 else 1
                                val validWeekNumber = when {
                                    weekNumber < 1 -> 1
                                    weekNumber > settings.totalWeeks -> settings.totalWeeks
                                    else -> weekNumber
                                }
                                userSelectedWeek = validWeekNumber

                            }
                        )
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = daysOfWeek[i],
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.date_format, dayDate.monthValue, dayDate.dayOfMonth),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.weight(0.5f)
            ) {
                Box(modifier = Modifier.height(0.dp))
                
                for (period in 1..settings.numberOfPeriods) {
                    // 移除额外的Box包装，让PeriodCell直接控制大小
                    PeriodCell(
                        period = period,
                        timetableSchedules = timetableSchedules, // 传递作息时间表数据
                        rowSpan = 1,
                        rowHeight = rowHeight
                    )

                    // 在PeriodCell之间添加水平分隔线（除了最后一个）
                    if (period < settings.numberOfPeriods) {
                        Divider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // 在Period列和课程列之间添加垂直分隔线
            Divider(
                modifier = Modifier
                    .width(0.5.dp)
                    .fillMaxHeight(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            for (day in 1..numberOfDays) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Box(modifier = Modifier.height(0.dp))
                    
                    for (period in 1..settings.numberOfPeriods) {
                        val courseInfo = displaySchedule[Pair(period, day)]
                        if (courseInfo != null && courseInfo.shouldShow && courseInfo.isStart) {
                            // 移除额外的Box包装，让CourseCell直接控制大小
                            CourseCell(
                                course = courseInfo.course,
                                schedule = courseInfo.schedule,
                                teacher = courseInfo.teacher,
                                location = courseInfo.location,
                                rowSpan = courseInfo.rowSpan,
                                rowHeight = rowHeight
                            )
                        } else if (courseInfo != null && !courseInfo.shouldShow && courseInfo.rowSpan > 1) {
                            // 被跨越的格子，不显示任何内容
                        } else {
                            // 显示空的课程格子以保持一致的大小
                            CourseCell(
                                course = Course("", "", 0.0, timetableId = 0),
                                schedule = null,
                                teacher = null,
                                location = null,
                                rowSpan = 1,
                                rowHeight = rowHeight
                            )
                        }

                        // 在CourseCell之间添加水平分隔线（除了最后一个）
                        if (period < settings.numberOfPeriods) {
                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                // 在列之间添加垂直分隔线（除了最后一列）
                if (day < numberOfDays) {
                    Divider(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight(),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

data class CourseDisplayInfo(
    val course: Course,
    val schedule: Schedule?,
    val teacher: Teacher?,
    val location: Location?,
    val shouldShow: Boolean,
    val isStart: Boolean = false,
    val rowSpan: Int = 1
)

fun createDisplaySchedule(
    courses: List<Course>,
    schedules: List<Schedule>,
    teachers: List<Teacher>,
    locations: List<Location>,
    numberOfPeriods: Int,
    numberOfDays: Int,
    currentWeek: Int
): Map<Pair<Int, Int>, CourseDisplayInfo> {
    
    val displaySchedule = mutableMapOf<Pair<Int, Int>, CourseDisplayInfo>()

    // 初始化所有位置
    for (period in 1..numberOfPeriods) {
        for (day in 1..numberOfDays) {
            displaySchedule[Pair(period, day)] = CourseDisplayInfo(
                course = Course("", "", 0.0, timetableId = 0),
                schedule = null,
                teacher = null,
                location = null,
                shouldShow = false
            )
        }
    }

    
    // 创建课程ID到课程的映射，方便快速查找
    val courseMap = courses.associateBy { it.id }

    // 处理课程数据
    schedules.forEach { schedule ->
        // 检查课程是否存在
        val course = courseMap[schedule.courseId]
        if (course == null) {
            return@forEach
        }
        
        // 检查当前周是否在课程的周次列表中
        if (currentWeek in schedule.weeks && schedule.dayOfWeek in 1..numberOfDays) {
            val day = schedule.dayOfWeek
            val startPeriod = schedule.startPeriod
            val endPeriod = schedule.endPeriod
            val rowSpan = endPeriod - startPeriod + 1


            // 确保节次在有效范围内
            if (startPeriod in 1..numberOfPeriods && endPeriod in startPeriod..numberOfPeriods) {
                // 查找教师和地点信息
                val teacher = teachers.find { it.id == schedule.teacherId }
                val location = locations.find { it.id == schedule.locationId }

                // 在起始位置显示课程卡片
                displaySchedule[Pair(startPeriod, day)] = CourseDisplayInfo(
                    course = course,
                    schedule = schedule,
                    teacher = teacher,
                    location = location,
                    shouldShow = true,
                    isStart = true,
                    rowSpan = rowSpan
                )

                // 标记连续课程的其他节次位置为不显示
                for (p in (startPeriod + 1)..endPeriod) {
                    displaySchedule[Pair(p, day)] = CourseDisplayInfo(
                        course = course,
                        schedule = schedule,
                        teacher = teacher,
                        location = location,
                        shouldShow = false,
                        isStart = false,
                        rowSpan = rowSpan
                    )
                }

            }

        }
    }

//    val shownCourses = displaySchedule.filter { it.value.shouldShow }

    return displaySchedule
}

@Composable
fun PeriodCell(
    period: Int,
    timetableSchedules: List<TimetableSchedule>,
    rowSpan: Int = 1,
    rowHeight: Dp
) {
    // 查找当前节次的作息时间
    val periodSchedule = timetableSchedules.find { 
        it.name == stringResource(R.string.period_number, period) || it.sortOrder == period 
    }

    // 使用淡色背景的Box替代Card组件，移除内边距以消除缝隙
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight * rowSpan)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)) // 淡色蒙版背景
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp), // 仅保留内容的内边距
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 第一行：显示"节次"
                Text(
                    text = stringResource(R.string.period_format, period),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 第二行：显示上课时间
                if (periodSchedule != null) {
                    Text(
                        text = periodSchedule.startTime.toString().substring(0, 5),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "--:--",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // 第三行：显示下课时间
                if (periodSchedule != null) {
                    Text(
                        text = periodSchedule.endTime.toString().substring(0, 5),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "--:--",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}