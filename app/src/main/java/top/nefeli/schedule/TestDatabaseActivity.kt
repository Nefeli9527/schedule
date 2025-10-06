package top.nefeli.schedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.model.Adjust
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.Timetable
import java.time.LocalTime

class TestDatabaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = ScheduleRepository(this)
        
        setContent {
            TestDatabaseScreen(repository)
        }
    }
}

@Composable
fun TestDatabaseScreen(repository: ScheduleRepository) {
    val context = LocalContext.current as ComponentActivity
    var testResult by remember { mutableStateOf("") }
    
    fun runTest() {
        context.lifecycleScope.launch {
            try {
                // 创建示例数据
                val timetable = Timetable(
                    name = "2023级计算机1班课表",
                    semester = "2023-2024-1"
                )
                val timetableId = repository.createTimetable(timetable)
                
                val course = Course(
                    name = "高等数学",
                    type = "必修",
                    credit = 4.0,
                    examTime = "2024-01-10 09:00",
                    note = "期末考试",
                    timetableId = timetableId
                )
                val courseId = repository.addCourse(course)
                
                val teacher = Teacher(name = "张教授")
                val teacherId = repository.addTeacher(teacher)
                
                val location = Location(
                    campus = "主校区",
                    building = "A栋",
                    classroom = "A101"
                )
                val locationId = repository.addLocation(location)

                val periods = listOf(
                    Period(
                        name = "第1节课",
                        startTime = LocalTime.of(8, 0),
                        endTime = LocalTime.of(8, 45),
                        periodType = "上午",
                        sortOrder = 1
                    ),
                    Period(
                        name = "第2节课",
                        startTime = LocalTime.of(8, 55),
                        endTime = LocalTime.of(9, 40),
                        periodType = "上午",
                        sortOrder = 2
                    )
                )
                
                val scheduleIds = mutableListOf<Long>()
                for (schedule in periods) {
                    scheduleIds.add(repository.addPeriod(schedule))
                }
                
                val schedule = Schedule(
                    courseId = courseId,
                    weeks = setOf(1, 2, 3, 4, 5),
                    dayOfWeek = 1, // 星期一
                    startPeriod = 1, // 使用直接的节次号而不是ID
                    endPeriod = 2,   // 使用直接的节次号而不是ID
                    locationId = locationId,
                    teacherId = teacherId
                )
                repository.addSchedule(schedule)
                
                val adjustment = Adjust(
                    date = java.time.LocalDate.now(),
                    scheduleId = schedule.id,
                    targetDate = java.time.LocalDate.now().plusDays(1),
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(11, 30),
                    originalPeriodId = null,
                    adjustType = "调课",
                    note = "教室变更"
                )
                repository.addAdjustment(adjustment)
                
                testResult = "测试成功！已创建示例数据。"
            } catch (e: Exception) {
                testResult = "测试失败: ${e.message}"
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { runTest() }) {
            Text("运行数据库测试")
        }
        Text(testResult)
    }
}