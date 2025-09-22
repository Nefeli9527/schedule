package top.nefeli.schedule.view.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.view.components.CourseScheduleInfo
import top.nefeli.schedule.view.components.EditableCourseSchedule
import top.nefeli.schedule.view.dialogs.LocationInputDialog
import top.nefeli.schedule.view.dialogs.TeacherInputDialog
import top.nefeli.schedule.view.dialogs.TimeSelectionDialog
import top.nefeli.schedule.view.dialogs.WeekSelectionDialog
import top.nefeli.schedule.viewmodel.ScheduleViewModel

@SuppressLint("MutableCollectionMutableState", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    onBack: () -> Unit,
    onAddCourse: (Course, List<Schedule>) -> Unit, // 更新为新的数据类型
    totalWeeks: Int, // 从设置中传入总周数
    timetableId: Long, // 当前课表ID
    initialCourse: Course? = null, // 可选的初始课程参数，用于编辑模式
    initialSchedules: List<Schedule> = emptyList(), // 初始课程时间安排
    viewModel: ScheduleViewModel = viewModel(), // 添加 ViewModel 参数
) {
    var courseName by remember { mutableStateOf(initialCourse?.name ?: "") }
    var credit by remember { mutableStateOf(initialCourse?.credit?.toString() ?: "") }
    var examTime by remember { mutableStateOf(initialCourse?.examTime ?: "") }
    var note by remember { mutableStateOf(initialCourse?.note ?: "") }
    val schedules by remember { mutableStateOf(initialSchedules.toMutableList()) }

    var csi by remember { mutableStateOf(mutableListOf<CourseScheduleInfo>()) }
    // 修复：使用 mutableStateOf 并添加 setter 来触发重组
    var index by remember { mutableStateOf(csi.size) }

//    var selectedDay by remember { mutableIntStateOf(1) }
//    var selectedStartPeriod by remember { mutableIntStateOf(1) }
//    var selectedEndPeriod by remember { mutableIntStateOf(1) }
//    var selectedWeeks by remember { mutableStateOf((1..totalWeeks).toSet()) }
//    var selectedTeacher by remember { mutableStateOf("") }
//    var selectedLocation by remember { mutableStateOf("") }

    // 弹窗相关状态
    var showWeekDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showTeacherDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var editingScheduleIndex by remember { mutableIntStateOf(-1) }

    // 监听课程名称变化，自动填充学分和考试时间
    var lastSearchedCourseName by remember { mutableStateOf("") }
    var existingCourseId by remember { mutableStateOf<Long?>(null) }

    // 提取的更新函数，用于更新数据库中的课程时间安排
    val updateScheduleInDatabase: (Int) -> Unit = { index ->
        Log.d("AddCourseScreen", "Updating schedule in database at index: $index")
        Log.d("AddCourseScreen", "Schedule info: ${csi[index]}")

        // 确保外键引用有效
        var locationId = csi[index].locationId
        if (locationId <= 0) {
            val locations = viewModel.findAllLocations()
            locationId = if (locations.isNotEmpty()) locations[0].id else 1
            Log.d("AddCourseScreen", "Fixed locationId to: $locationId")
        }

        var teacherId = csi[index].teacherId
        if (teacherId <= 0) {
            val teachers = viewModel.findAllTeachers()
            teacherId = if (teachers.isNotEmpty()) teachers[0].id else 1
            Log.d("AddCourseScreen", "Fixed teacherId to: $teacherId")
        }

        // 获取当前课程的ID
        val courseId = existingCourseId ?: (initialCourse?.id ?: 0)
        Log.d("AddCourseScreen", "Using courseId: $courseId")

        // 使用 CourseScheduleInfo 的 toSchedule 方法创建 Schedule 对象
        val scheduleToUpdate = csi[index].copy(
            courseId = courseId,
            locationId = locationId,
            teacherId = teacherId
        ).toSchedule()

        Log.d("AddCourseScreen", "Schedule to update: $scheduleToUpdate")
        // 调用ViewModel的方法来更新记录
        viewModel.updateSchedule(scheduleToUpdate)
        Log.d("AddCourseScreen", "Schedule updated successfully")
    }

    // 保存课程和时间安排的函数
    val saveCourseAndSchedules = {
        Log.d("AddCourseScreen", "saveCourseAndSchedules called")
        Log.d("AddCourseScreen", "Course name: $courseName")
        Log.d("AddCourseScreen", "Existing course ID: $existingCourseId")
        Log.d("AddCourseScreen", "Timetable ID: $timetableId")
        Log.d("AddCourseScreen", "CSI size: ${csi.size}")
        
        // 收集所有有效的课程时间安排
        val validSchedules = mutableListOf<Schedule>()

        for (i in 0 until csi.size) {
            Log.d("AddCourseScreen", "Processing schedule $i: ${csi[i]}")
            if (csi[i].weeks.isNotEmpty()) {
                // 确保地点和教师存在
                var locationId = csi[i].locationId
                if (locationId <= 0) {
                    Log.d(
                        "AddCourseScreen",
                        "Location ID is invalid, looking up or creating location"
                    )
                    // 检查地点是否已存在
                    val existingLocation = viewModel.findLocationByInfo(csi[i].location)
                    locationId = if (existingLocation != null) {
                        Log.d(
                            "AddCourseScreen",
                            "Found existing location with ID: ${existingLocation.id}"
                        )
                        existingLocation.id
                    } else {
                        // 地点不存在，创建新记录
                        viewModel.addLocation(csi[i].location)
                        // 重新查询获取ID（在实际应用中应通过返回值获取）
                        val newLocationId =
                            viewModel.findLocationByInfo(csi[i].location)?.id ?: 1 // 使用默认ID 1
                        Log.d("AddCourseScreen", "Created new location with ID: $newLocationId")
                        newLocationId
                    }
                    csi[i].locationId = locationId
                }

                var teacherId = csi[i].teacherId
                if (teacherId <= 0) {
                    Log.d(
                        "AddCourseScreen",
                        "Teacher ID is invalid, looking up or creating teacher"
                    )
                    // 检查教师是否已存在
                    val existingTeacher = viewModel.findTeacherByName(csi[i].teacher)
                    teacherId = if (existingTeacher != null) {
                        Log.d(
                            "AddCourseScreen",
                            "Found existing teacher with ID: ${existingTeacher.id}"
                        )
                        existingTeacher.id
                    } else {
                        // 教师不存在，创建新记录
                        viewModel.addTeacher(Teacher(csi[i].teacher))
                        // 重新查询获取ID（在实际应用中应通过返回值获取）
                        val newTeacherId =
                            viewModel.findTeacherByName(csi[i].teacher)?.id ?: 1 // 使用默认ID 1
                        Log.d("AddCourseScreen", "Created new teacher with ID: $newTeacherId")
                        newTeacherId
                    }
                    csi[i].teacherId = teacherId
                }

                // 获取课程ID
                val courseId = existingCourseId ?: timetableId
                Log.d("AddCourseScreen", "Using course ID: $courseId")

                // 使用 CourseScheduleInfo 的 toSchedule 方法创建 Schedule 对象
                val newSchedule = csi[i].copy(
                    courseId = courseId,
                    locationId = locationId,
                    teacherId = teacherId
                ).toSchedule().apply {
                    id = csi[i].id  // 保留原有的ID
                }
                Log.d("AddCourseScreen", "Adding valid schedule: $newSchedule")
                validSchedules.add(newSchedule)
            } else {
                Log.d("AddCourseScreen", "Schedule $i has no weeks selected, skipping")
            }
        }
        Log.d("AddCourseScreen", "Total valid schedules: ${validSchedules.size}")

        // 只有当有有效数据且timetableId有效时才添加课程
        if (courseName.isNotBlank() && validSchedules.isNotEmpty() && timetableId > 0) {
            val course = Course(
                name = courseName,
                type = "必修",
                credit = credit.toDoubleOrNull() ?: 0.0,
                examTime = examTime,
                note = note,
                timetableId = timetableId
            )
            Log.d("AddCourseScreen", "Creating course: $course")

            Log.d("AddCourseScreen", "Calling onAddCourse with ${validSchedules.size} schedules")
            // 只调用一次回调
            onAddCourse(course, validSchedules.toList())
            onBack()
        } else {
            Log.d(
                "AddCourseScreen", "Not creating course - name blank: ${courseName.isBlank()}, " +
                        "no valid schedules: ${validSchedules.isEmpty()}, timetableId invalid: ${timetableId <= 0}"
            )
        }
    }


    LaunchedEffect(courseName) {
        // 只有当课程名称不为空且发生变化时才进行搜索
        if (courseName.isNotEmpty() && courseName != lastSearchedCourseName && initialCourse == null) {
            // 在协程中调用 ViewModel 的挂起函数
            val existingCourses = viewModel.findCourseByName(courseName)
            if (existingCourses.isNotEmpty()) {
                // 使用找到的第一个课程的信息来填充字段
                val existingCourse = existingCourses[0]
                credit = existingCourse.credit.toString()
                examTime = existingCourse.examTime
                note = existingCourse.note
                // 保存现有课程的ID
                existingCourseId = existingCourse.id
                // 填充课程时间表信息
                val courseSchedules = viewModel.findSchedulesByCourse(existingCourse.id)
                Log.d(
                    "AddCourseScreen",
                    "Found ${courseSchedules.size} existing schedules for course ${existingCourse.name}"
                )
                csi = courseSchedules.map { schedule ->
                    val teacher = viewModel.findTeacherById(schedule.teacherId)
                    val location = viewModel.findLocationById(schedule.locationId)
                    val info = CourseScheduleInfo(schedule).apply {
                        this.teacher = teacher?.name ?: ""
                        this.location = location ?: Location()
                    }
                    Log.d("AddCourseScreen", "Mapped schedule info: $info")
                    info
                }.toMutableList()
                // 更新index以触发重组
                index = csi.size
            } else {
                // 如果没有找到现有课程，清除保存的课程ID
                existingCourseId = null
            }
            lastSearchedCourseName = courseName
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (initialCourse != null) "编辑课程" else stringResource(R.string.add_course_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                // 触发保存操作
                                saveCourseAndSchedules()
                            }
                        ) {
                            Text(
                                text = "保存",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // 添加新的课程时间安排并更新 index 以触发重组
                        Log.d("AddCourseScreen", "Adding new schedule, current count: ${csi.size}")
                        // 如果csi不为空，复制最后一个CourseScheduleInfo，否则创建新的
                        val newScheduleInfo = if (csi.isNotEmpty()) {
                            Log.d("AddCourseScreen", "Copying last schedule info")
                            csi.last().copy()
                        } else {
                            Log.d("AddCourseScreen", "Creating new schedule info")
                            CourseScheduleInfo(existingCourseId ?: -1)
                        }
                        // 确保新添加的安排ID为-1，表示是新的安排
                        newScheduleInfo.id = -1
                        csi.add(newScheduleInfo)
                        index = csi.size
                        Log.d("AddCourseScreen", "New schedule added, new count: ${csi.size}")

                        // 如果是编辑现有课程，立即保存新添加的时间安排到数据库
                        if (initialCourse != null && existingCourseId != null) {
                            Log.d("AddCourseScreen", "Adding schedule to existing course")
                            // 确保地点和教师存在
                            var locationId = newScheduleInfo.locationId
                            if (locationId <= 0) {
                                Log.d(
                                    "AddCourseScreen",
                                    "Location ID is invalid, looking up or creating location"
                                )
                                // 检查地点是否已存在
                                val existingLocation =
                                    viewModel.findLocationByInfo(newScheduleInfo.location)
                                locationId = if (existingLocation != null) {
                                    Log.d(
                                        "AddCourseScreen",
                                        "Found existing location with ID: ${existingLocation.id}"
                                    )
                                    existingLocation.id
                                } else {
                                    // 地点不存在，创建新记录
                                    viewModel.addLocation(newScheduleInfo.location)
                                    // 重新查询获取ID
                                    val newLocationId =
                                        viewModel.findLocationByInfo(newScheduleInfo.location)?.id
                                            ?: 1
                                    Log.d(
                                        "AddCourseScreen",
                                        "Created new location with ID: $newLocationId"
                                    )
                                    newLocationId
                                }
                                newScheduleInfo.locationId = locationId
                            }

                            var teacherId = newScheduleInfo.teacherId
                            if (teacherId <= 0) {
                                Log.d(
                                    "AddCourseScreen",
                                    "Teacher ID is invalid, looking up or creating teacher"
                                )
                                // 检查教师是否已存在
                                val existingTeacher =
                                    viewModel.findTeacherByName(newScheduleInfo.teacher)
                                teacherId = if (existingTeacher != null) {
                                    Log.d(
                                        "AddCourseScreen",
                                        "Found existing teacher with ID: ${existingTeacher.id}"
                                    )
                                    existingTeacher.id
                                } else {
                                    // 教师不存在，创建新记录
                                    viewModel.addTeacher(Teacher(newScheduleInfo.teacher))
                                    // 重新查询获取ID
                                    val newTeacherId =
                                        viewModel.findTeacherByName(newScheduleInfo.teacher)?.id
                                            ?: 1
                                    Log.d(
                                        "AddCourseScreen",
                                        "Created new teacher with ID: $newTeacherId"
                                    )
                                    newTeacherId
                                }
                                newScheduleInfo.teacherId = teacherId
                            }

                            // 创建并保存新的时间安排
                            val scheduleToAdd = newScheduleInfo.copy(
                                courseId = existingCourseId ?: 0,
                                locationId = locationId,
                                teacherId = teacherId
                            ).toSchedule()

                            Log.d("AddCourseScreen", "Adding schedule to database: $scheduleToAdd")
                            // 保存到数据库
                            viewModel.addSchedule(scheduleToAdd)
                            // 注意：由于在Composable中无法获取返回的ID，我们无法更新newScheduleInfo.id
                            // 但在实时更新的场景中，这不会造成太大问题
                            Log.d("AddCourseScreen", "Schedule added to database")
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加课程时间")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text(stringResource(R.string.course_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = credit,
                    onValueChange = { credit = it },
                    label = { Text("学分") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = examTime,
                    onValueChange = { examTime = it },
                    label = { Text("考试时间") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Log.d("","index:$index")

                // 修复：始终确保至少有一个课程时间安排
                if (csi.isEmpty()) {
                    csi.add(CourseScheduleInfo(existingCourseId ?: -1))
                    index = csi.size
                }

                for (i in 0 until csi.size) {
                    Log.d("AddCourseScreen", "Rendering schedule $i: ${csi[i]}")
                    EditableCourseSchedule(
                        day = csi[i].dayOfWeek,
                        period = csi[i].startPeriod,
                        endPeriod = csi[i].endPeriod,
                        weeks = csi[i].weeks,
                        teacher = csi[i].teacher,
                        location = csi[i].location.toString(),
                        onEditWeeks = {
                            showWeekDialog = true
                            editingScheduleIndex = i
                        },
                        onEditTime = {
                            showTimeDialog = true
                            editingScheduleIndex = i
                        },
                        onEditTeacher = {
                            showTeacherDialog = true
                            editingScheduleIndex = i
                        },
                        onEditLocation = {
                            showLocationDialog = true
                            editingScheduleIndex = i
                        },
                        onDelete = {
                            // 如果是已有记录（id > 0），需要从数据库中删除
                            if (csi[i].id > 0) {
                                Log.d(
                                    "AddCourseScreen",
                                    "Deleting schedule from database, ID: ${csi[i].id}"
                                )
                                // 创建一个Schedule对象用于删除
                                val scheduleToDelete = csi[i].toSchedule().apply {
                                    id = csi[i].id
                                }
                                // 调用ViewModel的方法来删除记录
                                viewModel.deleteSchedule(scheduleToDelete)
                                Log.d("AddCourseScreen", "Schedule deleted from database")
                            }
                            // 修复：正确地从 csi 中移除元素
                            csi.removeAt(i)
                            // 更新 index 以触发重组
                            index = csi.size
                            Log.d(
                                "AddCourseScreen",
                                "Deleted schedule at index $i, new size: ${csi.size}"
                            )
                        },
                        id = csi[i].id
                    )
                }

                // 移除原来的添加按钮，使用悬浮按钮替代

                Spacer(modifier = Modifier.height(72.dp))

                // 显示当前正在编辑的课程时间
            }
        }

        // 弹窗组件
        if (showWeekDialog) {
            val currentWeeks = csi[editingScheduleIndex].weeks

            WeekSelectionDialog(
                totalWeeks = totalWeeks,
                selectedWeeks = currentWeeks,
                onConfirm = { weeks ->
                    if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                        // 更新已添加的课程时间
                        val updatedSchedule = schedules[editingScheduleIndex].copy(weeks = weeks)
                        schedules[editingScheduleIndex] = updatedSchedule
                    } else {
                        // 更新当前选择的课程时间
                        csi[editingScheduleIndex].weeks = weeks
                        // 如果是已有记录（id > 0），需要更新数据库中的记录
                        if (csi[editingScheduleIndex].id > 0) {
                            // 调用提取的更新函数
                            updateScheduleInDatabase(editingScheduleIndex)
                        }
                    }
                },
                onDismiss = {
                    showWeekDialog = false
                    editingScheduleIndex = -1
                }
            )
        }

        if (showTimeDialog) {
            val currentDay =
                if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                    schedules[editingScheduleIndex].dayOfWeek
                } else {
                    csi[editingScheduleIndex].dayOfWeek
                }

            val currentStartPeriod =
                if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                    schedules[editingScheduleIndex].startPeriod
                } else {
                    csi[editingScheduleIndex].startPeriod
                }

            val currentEndPeriod =
                if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                    schedules[editingScheduleIndex].endPeriod
                } else {
                    csi[editingScheduleIndex].endPeriod
                }

            TimeSelectionDialog(
                selectedDay = currentDay,
                selectedPeriod = currentStartPeriod,
                endPeriod = currentEndPeriod,
                onConfirm = { day, startPeriod, endPeriod ->
                    if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                        // 更新已添加的课程时间
                        val updatedSchedule = schedules[editingScheduleIndex].copy(
                            dayOfWeek = day,
                            startPeriod = startPeriod,
                            endPeriod = endPeriod
                        )
                        schedules[editingScheduleIndex] = updatedSchedule
                    } else {
                        // 更新当前选择的课程时间
                        csi[editingScheduleIndex].dayOfWeek = day
                        csi[editingScheduleIndex].startPeriod = startPeriod
                        csi[editingScheduleIndex].endPeriod = endPeriod
                        // 如果是已有记录（id > 0），需要更新数据库中的记录
                        if (csi[editingScheduleIndex].id > 0) {
                            // 调用提取的更新函数
                            updateScheduleInDatabase(editingScheduleIndex)
                        }
                    }
                },
                onDismiss = {
                    showTimeDialog = false
                    editingScheduleIndex = -1
                }
            )
        }

        if (showTeacherDialog) {
            val initialTeacher = csi[editingScheduleIndex].teacher
            TeacherInputDialog(
                initialTeacher = initialTeacher,
                onConfirm = { teacher ->
                    csi[editingScheduleIndex].teacher = teacher
                    // 查找或创建教师记录以获取ID
                    val existingTeacher = viewModel.findTeacherByName(teacher)
                    val teacherId = if (existingTeacher != null) {
                        // 教师已存在，使用现有ID
                        existingTeacher.id
                    } else {
                        // 教师不存在，创建新记录
                        viewModel.addTeacher(Teacher(teacher))
                        // 重新查询获取ID（在实际应用中应通过返回值获取）
                        viewModel.findTeacherByName(teacher)?.id ?: viewModel.findAllTeachers()
                            .firstOrNull()?.id ?: 1
                    }
                    csi[editingScheduleIndex].teacherId = teacherId

                    if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                        // 更新已添加的课程时间
                        val updatedSchedule =
                            schedules[editingScheduleIndex].copy(teacherId = teacherId)
                        schedules[editingScheduleIndex] = updatedSchedule
                    } else {
                        // 如果是已有记录（id > 0），需要更新数据库中的记录
                        if (csi[editingScheduleIndex].id > 0) {
                            // 调用提取的更新函数
                            updateScheduleInDatabase(editingScheduleIndex)
                        }
                    }
                },
                onDismiss = {
                    showTeacherDialog = false
                    editingScheduleIndex = -1
                }
            )
        }

        if (showLocationDialog) {
                LocationInputDialog(
                    initialLocation = csi[editingScheduleIndex].location.toString(),
                    onConfirm = { location -> 
                        // 解析输入的地点信息
                        val parts = location.split(" ")
                        var campus = ""
                        var building = ""
                        val classroom: String

                        if (parts.size >= 3) {
                            campus = parts[0]
                            building = parts[1]
                            classroom = parts[2]
                        } else if (parts.size == 2) {
                            building = parts[0]
                            classroom = parts[1]
                        } else {
                            classroom = parts[0]
                        }

                        val locationObj = Location(campus, building, classroom)
                        csi[editingScheduleIndex].location = locationObj
                        
                        // 查找或创建地点记录以获取ID
                        val existingLocation = viewModel.findLocationByInfo(locationObj)
                        val locationId = if (existingLocation != null) {
                            // 地点已存在，使用现有ID
                            existingLocation.id
                        } else {
                            // 地点不存在，创建新记录
                            viewModel.addLocation(locationObj)
                            // 重新查询获取ID（在实际应用中应通过返回值获取）
                            viewModel.findLocationByInfo(locationObj)?.id
                                ?: viewModel.findAllLocations().firstOrNull()?.id ?: 1
                        }
                        csi[editingScheduleIndex].locationId = locationId

                        if (editingScheduleIndex >= 0 && editingScheduleIndex < schedules.size) {
                            // 更新已添加的课程时间
                            val updatedSchedule =
                                schedules[editingScheduleIndex].copy(locationId = locationId)
                            schedules[editingScheduleIndex] = updatedSchedule
                        } else {
                            // 如果是已有记录（id > 0），需要更新数据库中的记录
                            if (csi[editingScheduleIndex].id > 0) {
                                // 调用提取的更新函数
                                updateScheduleInDatabase(editingScheduleIndex)
                            }
                        }
                    },
                    onDismiss = {
                        showLocationDialog = false
                        editingScheduleIndex = -1
                    }
                )
            }

    }

    /*FloatingActionButton(
        onClick = {
            // 添加新的课程时间安排并更新 index 以触发重组
            csi.add(CourseScheduleInfo())
            index = csi.size
        },
        modifier = Modifier
            .padding(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "添加课程时间")
    }*/
}

@Preview(showBackground = true)
@Composable
fun AddCourseScreenPreview() {
    AddCourseScreen(
        onBack = {},
        onAddCourse = { _, _ -> },
        totalWeeks = 20,
        timetableId = 1
    )
}