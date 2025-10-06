package top.nefeli.schedule.viewmodel

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import top.nefeli.schedule.data.ScheduleRepository
import top.nefeli.schedule.model.Adjust
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.Timetable

/**
 * 课程表 ViewModel 类
 * 负责管理课表相关的数据和业务逻辑
 * 使用 MVVM 架构模式，通过 StateFlow 向 UI 层暴露数据状态
 *
 * @param repository 数据仓库，用于访问数据库
 * @param context 应用上下文，用于访问 SharedPreferences 等系统服务
 */
class ScheduleViewModel(private val repository: ScheduleRepository, private val context: Context) : ViewModel() {
    // 当前选中的课表ID
    private val _currentTimetableId = MutableStateFlow<Long?>(null)
    val currentTimetableId: StateFlow<Long?> = _currentTimetableId.asStateFlow()

    // 课表列表
    private val _timetables = MutableStateFlow<List<Timetable>>(emptyList())
    val timetables: StateFlow<List<Timetable>> = _timetables.asStateFlow()

    // 当前课表的课程
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    // 时间安排
    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    // 地点列表
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    // 老师列表
    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    // 作息时间表
    private val _period = MutableStateFlow<List<Period>>(emptyList())
    val period: StateFlow<List<Period>> = _period.asStateFlow()

    // 调课记录
    private val _adjustments = MutableStateFlow<List<Adjust>>(emptyList())
    val adjustments: StateFlow<List<Adjust>> = _adjustments.asStateFlow()

    // 快速缓存是否已加载
    private val _isQuickCacheLoaded = MutableStateFlow(false)
    val isQuickCacheLoaded: StateFlow<Boolean> = _isQuickCacheLoaded.asStateFlow()

    /**
     * 初始化函数
     * 在 ViewModel 创建时加载所有必要数据
     */
    init {
        loadTimetables()
        loadLocations()
        loadTeachers()
        loadPeriods()
        loadAdjustments()
        loadQuickCache()
        
        // 初始化默认的作息时间表
        viewModelScope.launch {
            repository.initializeDefaultPeriods()
            // 重新加载作息时间表数据以确保UI更新
            loadPeriods()
        }
    }

    /**
     * 加载所有课表数据
     */
    private fun loadTimetables() {
        viewModelScope.launch {
            _timetables.value = repository.getAllTimetables()
            // 如果当前没有选中课表且存在课表，则选中第一个
            if (_currentTimetableId.value == null && _timetables.value.isNotEmpty()) {
                selectTimetable(_timetables.value.first().id)
            } else if (_timetables.value.isNotEmpty()) {
                // 如果已经有选中的课表，重新加载该课表的数据
                _currentTimetableId.value?.let { timetableId ->
                    loadCoursesForTimetable(timetableId)
                    loadSchedules()
                }
            }
        }
    }

    /**
     * 加载所有地点数据
     */
    private fun loadLocations() {
        viewModelScope.launch {
            _locations.value = repository.getAllLocations()
        }
    }

    /**
     * 加载所有教师数据
     */
    private fun loadTeachers() {
        viewModelScope.launch {
            _teachers.value = repository.getAllTeachers()
        }
    }

    /**
     * 加载所有作息时间表数据
     */
    private fun loadPeriods() {
        viewModelScope.launch {
            _period.value = repository.getAllPeriods()
        }
    }

    /**
     * 加载调课记录数据
     */
    private fun loadAdjustments() {
        viewModelScope.launch {
            _adjustments.value = repository.loadAdjustments()
        }
    }

    // 加载快速缓存数据
    private fun loadQuickCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
                val cachedData = prefs.getString("current_schedule_data", null)

                if (cachedData != null) {
                    val json = JSONObject(cachedData)
                    val timetableId = json.getLong("timetableId")


                    // 加载缓存的课程数据
                    val coursesJson = json.getJSONArray("courses")
                    val cachedCourses = mutableListOf<Course>()
                    for (i in 0 until coursesJson.length()) {
                        val courseJson = coursesJson.getJSONObject(i)
                        val course = Course(
                                name = courseJson.getString("name"),
                                type = courseJson.getString("type"),
                                credit = courseJson.getDouble("credit"),
                                examTime = courseJson.getString("examTime"),
                                note = courseJson.getString("note"),
                                timetableId = courseJson.getLong("timetableId"),
                            ).apply {
                                id = courseJson.getLong("id")
                            }
                        cachedCourses.add(course)
                    }

                    // 加载缓存的时间安排数据
                    val schedulesJson = json.getJSONArray("schedules")
                    val cachedSchedules = mutableListOf<Schedule>()
                    for (i in 0 until schedulesJson.length()) {
                        val scheduleJson = schedulesJson.getJSONObject(i)
                        val weeksArray = scheduleJson.getJSONArray("weeks")
                        val weeks = mutableSetOf<Int>()
                        for (j in 0 until weeksArray.length()) {
                            weeks.add(weeksArray.getInt(j))
                        }

                        val schedule = Schedule(
                                courseId = scheduleJson.getLong("courseId"),
                                weeks = weeks,
                                dayOfWeek = scheduleJson.getInt("dayOfWeek"),
                                startPeriod = scheduleJson.getInt("startPeriod"),
                                endPeriod = scheduleJson.getInt("endPeriod"),
                                locationId = scheduleJson.getLong("locationId"),
                                teacherId = scheduleJson.getLong("teacherId"),
                            ).apply {
                                id = scheduleJson.getLong("id")
                            }
                        cachedSchedules.add(schedule)
                    }

                    // 更新UI线程中的状态
                    withContext(Dispatchers.Main) {
                        _currentTimetableId.value = timetableId
                        _courses.value = cachedCourses
                        _schedules.value = cachedSchedules
                        _isQuickCacheLoaded.value = true
                        
                        // 确保其他相关数据也被加载
                        loadLocations()
                        loadTeachers()
                        loadPeriods()
                        loadAdjustments()
                    }
                } else {
                    // 没有缓存数据时，从数据库加载最新数据并更新缓存
                    loadFromDatabaseAndRefreshCache()
                }
            } catch (e: Exception) {
                // 出错时也从数据库加载数据
                loadFromDatabaseAndRefreshCache()
            }
        }
    }

    // 从数据库加载最新数据并更新缓存
    private fun loadFromDatabaseAndRefreshCache() {
        viewModelScope.launch {
            loadTimetables()
            loadLocations()
            loadTeachers()
            loadPeriods()
            loadAdjustments()
        }
    }

    // 保存当前数据到快速缓存
    private fun saveToQuickCache(timetableId: Long, courses: List<Course>, schedules: List<Schedule>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)

                // 构建课程JSON数据
                val coursesJson = JSONArray()
                courses.forEach { course ->
                    val courseJson = JSONObject().apply {
                        put("id", course.id)
                        put("name", course.name)
                        put("type", course.type)
                        put("credit", course.credit)
                        put("examTime", course.examTime)
                        put("note", course.note)
                        put("timetableId", course.timetableId)
                    }
                    coursesJson.put(courseJson)
                }

                // 构建时间安排JSON数据
                val schedulesJson = JSONArray()
                schedules.forEach { schedule ->
                    val scheduleJson = JSONObject().apply {
                        put("id", schedule.id)
                        put("courseId", schedule.courseId)
                        put("dayOfWeek", schedule.dayOfWeek)
                        put("startPeriod", schedule.startPeriod)
                        put("endPeriod", schedule.endPeriod)
                        put("locationId", schedule.locationId)
                        put("teacherId", schedule.teacherId)

                        val weeksArray = JSONArray()
                        schedule.weeks.forEach { week ->
                            weeksArray.put(week)
                        }
                        put("weeks", weeksArray)
                    }
                    schedulesJson.put(scheduleJson)
                }

                // 构建完整JSON对象
                val jsonData = JSONObject().apply {
                    put("timetableId", timetableId)
                    put("courses", coursesJson)
                    put("schedules", schedulesJson)
                }

                // 保存到SharedPreferences
                prefs.edit {
                    putString("current_schedule_data", jsonData.toString())
                }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * 选择课表
     *
     * @param timetableId 课表ID
     */
    fun selectTimetable(timetableId: Long) {
        _currentTimetableId.value = timetableId
        loadCoursesForTimetable(timetableId)
        loadSchedules()
    }

    /**
     * 加载指定课表的所有课程
     *
     * @param timetableId 课表ID
     */
    private fun loadCoursesForTimetable(timetableId: Long) {
        viewModelScope.launch {
            val courses = repository.getCoursesByTimetable(timetableId)
            _courses.value = courses

            // 同时保存到快速缓存
            _currentTimetableId.value?.let { currentTimetableId ->
                if (currentTimetableId == timetableId) {
                    saveToQuickCache(timetableId, courses, _schedules.value)
                }
            }
        }
    }

    /**
     * 加载所有时间安排
     */
    private fun loadSchedules() {
        viewModelScope.launch {
            val schedules = _currentTimetableId.value?.let { timetableId ->
                repository.getSchedulesByTimetable(timetableId)
            } ?: emptyList()
            _schedules.value = schedules

            // 同时保存到快速缓存
            _currentTimetableId.value?.let { timetableId ->
                saveToQuickCache(timetableId, _courses.value, schedules)
            }
        }
    }

    /**
     * 创建新课表
     *
     * @param timetable 课表对象
     */
    fun createTimetable(timetable: Timetable) {
        viewModelScope.launch {
            val id = repository.createTimetable(timetable)
            loadTimetables()
            if (_timetables.value.size == 1) {
                // 如果这是第一个课表，自动选择它
                selectTimetable(id)
            }
        }
    }

    /**
     * 添加课程
     *
     * @param course 课程对象
     */
    fun addCourse(course: Course) {
        viewModelScope.launch {
            val courseId = repository.addCourse(course)
            _currentTimetableId.value?.let { timetableId ->
                loadCoursesForTimetable(timetableId)
            }
        }
    }

    /**
     * 添加课程及其时间安排
     *
     * @param course 课程对象
     * @param schedules 时间安排列表
     */
    fun addCourseWithSchedules(course: Course, schedules: List<Schedule>) {
        viewModelScope.launch {
            
            // 检查同一课表中是否已存在相同名称的课程
            val isCourseNameExists = _currentTimetableId.value?.let { timetableId ->
                repository.isCourseNameExistsInTimetable(timetableId, course.name)
            } ?: false

            if (isCourseNameExists) {
                // 如果课程名称已存在，添加新的时间安排到现有课程
                println("Course name already exists: ${course.name}, adding schedules to existing course")

                // 查找现有课程
                val existingCourse = _currentTimetableId.value?.let { timetableId ->
                    repository.getCourseByTimetableAndName(timetableId, course.name).firstOrNull()
                }

                if (existingCourse != null) {
                    // 获取现有课程的所有时间安排
                    val existingSchedules = repository.getSchedulesByCourse(existingCourse.id)

                    // 分离需要更新和需要添加的时间安排
                    val (schedulesToUpdate, schedulesToAdd) = schedules.partition { it.id > 0 }

                    // 更新已存在的时间安排
                    for (schedule in schedulesToUpdate) {
                        // 检查该时间安排是否真的存在于数据库中
                        val existsInDb = existingSchedules.any { it.id == schedule.id }
                        if (existsInDb) {
                            val updatedSchedule = schedule.copy(courseId = existingCourse.id)
                            println("Updating existing schedule: $updatedSchedule")
                            try {
                                repository.updateSchedule(updatedSchedule)
                            } catch (e: Exception) {
                                println("Error updating schedule: ${e.message}")
                            }
                        } else {
                            // 如果不存在于数据库中，当作新安排添加
                            val newSchedule = schedule.copy(courseId = existingCourse.id)
                            println("Adding new schedule to existing course: $newSchedule")
                            try {
                                val scheduleId = repository.addSchedule(newSchedule)
                                println("Added schedule with ID: $scheduleId")
                            } catch (e: Exception) {
                                println("Error adding schedule: ${e.message}")
                            }
                        }
                    }

                    // 添加新的时间安排
                    for (schedule in schedulesToAdd) {
                        val newSchedule = schedule.copy(courseId = existingCourse.id)
                        println("Adding new schedule to existing course: $newSchedule")
                        try {
                            val scheduleId = repository.addSchedule(newSchedule)
                            println("Added schedule with ID: $scheduleId")
                        } catch (e: Exception) {
                            println("Error adding schedule: ${e.message}")
                        }
                    }

                    // 刷新数据
                    _currentTimetableId.value?.let { timetableId ->
                        loadCoursesForTimetable(timetableId)
                    }
                    loadSchedules()
                }
                return@launch
            }

            println("Adding new course: $course")
            // 添加课程并获取ID
            val courseId = try {
                repository.addCourse(course)
            } catch (e: Exception) {
                println("Error adding course: ${e.message}")
                return@launch
            }
            println("Added course with ID: $courseId")

            // 更新所有时间安排的课程ID
            val updatedSchedules = schedules.map { schedule ->
                val updated = schedule.copy(courseId = courseId)
                println("Updating schedule with course ID: $updated")
                updated
            }

            // 添加所有时间安排
            for (schedule in updatedSchedules) {
                println("Adding schedule: $schedule")
                try {
                    val scheduleId = repository.addSchedule(schedule)
                    println("Added schedule with ID: $scheduleId")
                } catch (e: Exception) {
                    println("Error adding schedule: ${e.message}")
                }
            }

            // 刷新数据
            _currentTimetableId.value?.let { timetableId ->
                loadCoursesForTimetable(timetableId)
            }
            loadSchedules()
        }
    }

    /**
     * 更新课程信息
     *
     * @param course 需要更新的课程对象
     */
    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            _currentTimetableId.value?.let { timetableId ->
                loadCoursesForTimetable(timetableId)
            }
        }
    }

    /**
     * 更新课程的时间安排（处理增删改）
     *
     * @param courseId 课程ID
     * @param newSchedules 新的时间安排列表
     */
    fun updateCourseSchedules(courseId: Long, newSchedules: List<Schedule>) {
        viewModelScope.launch {
            // 获取数据库中现有的时间安排
            val existingSchedules = repository.getSchedulesByCourse(courseId)

            // 处理时间安排的增删改
            // 1. 确定需要删除的时间安排（在数据库中但不在新列表中）
            val scheduleIdsToKeep =
                newSchedules.mapNotNull { if (it.id > 0) it.id else null }.toSet()
            val schedulesToDelete = existingSchedules.filter { it.id !in scheduleIdsToKeep }
            schedulesToDelete.forEach { schedule ->
                repository.deleteSchedule(schedule)
            }

            // 2. 更新或添加时间安排
            newSchedules.forEach { schedule ->
                if (schedule.id > 0) {
                    // 更新现有时间安排
                    repository.updateSchedule(schedule)
                } else {
                    // 添加新的时间安排
                    repository.addSchedule(schedule)
                }
            }

            // 刷新数据
            loadSchedules()
        }
    }

    /**
     * 删除课程
     *
     * @param course 需要删除的课程对象
     */
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            _currentTimetableId.value?.let { timetableId ->
                loadCoursesForTimetable(timetableId)
            }
        }
    }

    /**
     * 重置指定时间表（删除所有相关数据）
     *
     * @param timetableId 时间表ID
     */
    fun resetTimetable(timetableId: Long) {
        viewModelScope.launch {
            repository.resetTimetable(timetableId)
            _currentTimetableId.value?.let { id ->
                if (id == timetableId) {
                    loadCoursesForTimetable(timetableId)
                    loadSchedules()
                }
            }
        }
    }

    /**
     * 添加时间安排
     *
     * @param schedule 时间安排对象
     */
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.addSchedule(schedule)
            loadSchedules()
        }
    }

    /**
     * 更新时间安排
     */
    fun updateSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
            loadSchedules()
        }
    }

    /**
     * 删除时间安排
     */
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            loadSchedules()
        }
    }

    fun findAllScheldules(): List<Schedule> {
        return _schedules.value
    }

    fun findAllTeachers(): List<Teacher> {
        return _teachers.value
    }

    fun findAllLocations(): List<Location> {
        return _locations.value
    }
    
    /**
     * 添加地点
     *
     * @param location 地点对象
     */
    fun addLocation(location: Location) {
        viewModelScope.launch {
            repository.addLocation(location)
            loadLocations()
        }
    }

    /**
     * 更新地点信息
     *
     * @param location 需要更新的地点对象
     */
    fun updateLocation(location: Location) {
        viewModelScope.launch {
            repository.updateLocation(location)
            loadLocations()
        }
    }

    /**
     * 删除地点
     *
     * @param location 需要删除的地点对象
     */
    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            repository.deleteLocation(location)
            loadLocations()
        }
    }

    /**
     * 根据地点ID获取地点详情
     */
    fun findLocationById(locationId: Long): Location? {
        if (locationId <= 0) return null
        for (loc in _locations.value) {
            if (loc.id == locationId) {
                return loc
            }
        }
        return null // 如果未找到匹配项，返回-1表示未找到
    }

    /**
     * 根据地点信息获取地点ID
     */
    fun findLocationByDetails(campus: String, building: String, classroom: String): Location?  {
        for (loc in _locations.value) {
            if (loc.campus == campus && loc.building == building && loc.classroom == classroom) {
                return loc
            }
        }
//        addLocation(location)
//        _locations.value.forEach{ it ->
//            if (it.campus == location.campus && it.building == location.building && it.classroom == location.classroom) {
//                return it
//            }
//        }
        return null // 如果未找到匹配项，返回-1表示未找到（该丢报错了）
    }
    /**
     * 根据地点信息获取地点ID
     */
    fun findLocationByInfo(location: Location): Location?  {
        for (loc in _locations.value) {
            if (loc.campus == location.campus && loc.building == location.building && loc.classroom == location.classroom) {
                return loc
            }
        }
//        addLocation(location)
//        _locations.value.forEach{ it ->
//            if (it.campus == location.campus && it.building == location.building && it.classroom == location.classroom) {
//                return it
//            }
//        }
        return null // 如果未找到匹配项，返回-1表示未找到（该丢报错了）
    }

    /**
     * 添加教师
     *
     * @param teacher 教师对象
     */
    fun addTeacher(teacher: String) {
        viewModelScope.launch {
            repository.addTeacher(Teacher(teacher))
            loadTeachers()
        }
    }

    /**
     * 添加教师
     *
     * @param teacher 教师对象
     */
    fun addTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.addTeacher(teacher)
            loadTeachers()
        }
    }

    /**
     * 更新教师信息
     *
     * @param teacher 需要更新的教师对象
     */
    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            loadTeachers()
        }
    }

    /**
     * 删除教师
     *
     * @param teacher 需要删除的教师对象
     */
    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            loadTeachers()
        }
    }

    /**
     * 根据教师ID获取教师名称
     */
    fun findTeacherById(teacherId: Long): Teacher? {
        if (teacherId <= 0) return null
        for (tea in _teachers.value) {
            if (tea.id == teacherId) {
                return tea
            }
        }
        return null // 如果未找到匹配项，返回-1表示未找到
    }

    /**
     * 根据教师名称获取教师ID
     */
    fun findTeacherByName(teacherName: String): Teacher? {
        if (teacherName.isEmpty()) return null
        for (tea in _teachers.value) {
            if (tea.name == teacherName) {
                return tea
            }
        }
//        addTeacher(Teacher(teacherName))
//        _teachers.value.forEach{ it ->
//            if (it.name == teacherName) {
//                return it
//            }
//        }
        return null // 如果未找到匹配项，返回-1表示未找到
    }

    /**
     * 添加作息时间安排
     *
     * @param schedule 作息时间安排对象
     */
    fun addPeriod(schedule: Period) {
        viewModelScope.launch {
            repository.addPeriod(schedule)
            loadPeriods()
        }
    }

    /**
     * 更新作息时间安排
     *
     * @param schedule 需要更新的作息时间安排对象
     */
    fun updatePeriod(schedule: Period) {
        viewModelScope.launch {
            repository.updatePeriod(schedule)
            loadPeriods()
        }
    }

    /**
     * 删除作息时间安排
     *
     * @param schedule 需要删除的作息时间安排对象
     */
    fun deletePeriod(schedule: Period) {
        viewModelScope.launch {
            repository.deletePeriod(schedule)
            loadPeriods()
        }
    }

    /**
     * 添加调课记录
     *
     * @param adjustment 调课记录对象
     */
    fun addAdjustment(adjustment: Adjust) {
        viewModelScope.launch {
            repository.addAdjustment(adjustment)
            loadAdjustments()
        }
    }

    /**
     * 更新调课记录
     *
     * @param adjustment 需要更新的调课记录对象
     */
    fun updateAdjustment(adjustment: Adjust) {
        viewModelScope.launch {
            repository.updateAdjustment(adjustment)
            loadAdjustments()
        }
    }

    /**
     * 删除调课记录
     */
    fun deleteAdjustment(adjustment: Adjust) {
        viewModelScope.launch {
            repository.deleteAdjustment(adjustment)
            loadAdjustments()
        }
    }

    /**
     * 初始化默认的作息时间表
     */
    fun initializeDefaultPeriods() {
        viewModelScope.launch {
            repository.initializeDefaultPeriods()
            loadPeriods()
        }
    }

    /**
     * 创建默认课表
     */
    fun createDefaultTimetable() {
        viewModelScope.launch {
            val timetableId = repository.createDefaultTimetable()
            // 更新课表列表
            loadTimetables()
            // 选择新创建的课表
            selectTimetable(timetableId)
        }
    }

    /**
     * 根据课程名称查找课程信息
     *
     * @param courseName 课程名称
     * @return 匹配的课程列表
     */
    suspend fun findCourseByName(courseName: String): List<Course> {
        val timetableId = _currentTimetableId.value ?: return emptyList()
        return repository.getCourseByTimetableAndName(timetableId, courseName)
    }

    /**
     * 根据课程ID查找课程安排
     *
     * @param courseId 课程ID
     * @return 匹配的课程安排列表
     */
    suspend fun findSchedulesByCourse(courseId: Long): List<Schedule> {
        return repository.getSchedulesByCourse(courseId)
    }
}

/**
 * ScheduleViewModel 的工厂类
 * 用于创建 ScheduleViewModel 实例
 *
 * @param repository 数据仓库
 * @param context 应用上下文
 */
class ScheduleViewModelFactory(private val repository: ScheduleRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
