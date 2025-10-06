package top.nefeli.schedule.data

import android.content.Context
import kotlinx.coroutines.flow.first
import top.nefeli.schedule.model.Adjust
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.Timetable

/**
 * 课程表数据仓库类
 * 负责课程数据的持久化存储和读取
 */
class ScheduleRepository(context: Context) {
    // 获取数据库实例
    private val database = ScheduleDatabase.getDatabase(context)
    private val courseDao = database.courseDao()
        
    // 用于存储调课数据的SharedPreferences
    private val adjustmentPreferences: android.content.SharedPreferences = 
        context.getSharedPreferences("adjustment_data", Context.MODE_PRIVATE)

    /**
     * 获取所有课表
     */
    suspend fun getAllTimetables(): List<Timetable> {
        return courseDao.getAllTimetables().first()
    }

    /**
     * 创建新课表
     */
    suspend fun createTimetable(timetable: Timetable): Long {
        return courseDao.insertTimetable(timetable)
    }

    /**
     * 获取当前课表的所有课程
     */
    suspend fun getCoursesByTimetable(timetableId: Long): List<Course> {
        return courseDao.getCoursesByTimetable(timetableId).first()
    }

    /**
     * 检查同一课表中是否已存在相同名称的课程
     *
     * @param timetableId 课表ID
     * @param courseName 课程名称
     * @return 如果存在返回true，否则返回false
     */
    suspend fun isCourseNameExistsInTimetable(timetableId: Long, courseName: String): Boolean {
        return courseDao.getCourseByTimetableAndName(timetableId, courseName).isNotEmpty()
    }

    /**
     * 根据课表ID和课程名称获取课程信息
     *
     * @param timetableId 课表ID
     * @param courseName 课程名称
     * @return 匹配的课程列表
     */
    suspend fun getCourseByTimetableAndName(timetableId: Long, courseName: String): List<Course> {
        return courseDao.getCourseByTimetableAndName(timetableId, courseName)
    }

    /**
     * 根据课程ID获取课程安排
     *
     * @param courseId 课程ID
     * @return 匹配的课程安排列表
     */
    suspend fun getSchedulesByCourse(courseId: Long): List<Schedule> {
        return courseDao.getSchedulesByCourse(courseId).first()
    }

    /**
     * 添加课程到课表
     */
    suspend fun addCourse(course: Course): Long {
        return courseDao.insertCourse(course)
    }

    /**
     * 更新课程
     */
    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course)
    }

    /**
     * 删除课程
     */
    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course)
    }

    /**
     * 获取所有时间安排
     */
    suspend fun getAllSchedules(): List<Schedule> {
        return courseDao.getAllSchedules().first()
    }

    /**
     * 获取指定课表的所有时间安排
     */
    suspend fun getSchedulesByTimetable(timetableId: Long): List<Schedule> {
        return courseDao.getSchedulesByTimetable(timetableId).first()
    }

    /**
     * 添加时间安排
     */
    suspend fun addSchedule(schedule: Schedule): Long {
        return courseDao.insertSchedule(schedule)
    }

    /**
     * 更新时间安排
     */
    suspend fun updateSchedule(schedule: Schedule) {
        courseDao.updateSchedule(schedule)
    }

    /**
     * 删除时间安排
     */
    suspend fun deleteSchedule(schedule: Schedule) {
        courseDao.deleteSchedule(schedule)
    }

    /**
     * 获取所有地点
     */
    suspend fun getAllLocations(): List<Location> {
        return courseDao.getAllLocations().first()
    }

    /**
     * 根据ID获取地点
     *
     * @param id 地点ID
     * @return 地点对象，如果不存在则返回null
     */
    suspend fun getLocationById(id: Long): Location? {
        return courseDao.getLocationById(id)
    }

    /**
     * 根据详细信息获取地点
     *
     * @param campus 校区
     * @param building 教学楼
     * @param classroom 教室
     * @return 地点对象，如果不存在则返回null
     */
    suspend fun getLocationByDetails(campus: String?, building: String?, classroom: String): Location? {
        return courseDao.getLocationByDetails(campus, building, classroom)
    }

    /**
     * 添加地点
     */
    suspend fun addLocation(location: Location): Long {
        return courseDao.insertLocation(location)
    }

    /**
     * 更新地点
     */
    suspend fun updateLocation(location: Location) {
        courseDao.updateLocation(location)
    }

    /**
     * 删除地点
     */
    suspend fun deleteLocation(location: Location) {
        courseDao.deleteLocation(location)
    }

    /**
     * 获取所有老师
     */
    suspend fun getAllTeachers(): List<Teacher> {
        return courseDao.getAllTeachers().first()
    }

    /**
     * 根据ID获取教师
     *
     * @param id 教师ID
     * @return 教师对象，如果不存在则返回null
     */
    suspend fun getTeacherById(id: Long): Teacher? {
        return courseDao.getTeacherById(id)
    }

    /**
     * 根据姓名获取教师
     *
     * @param name 教师姓名
     * @return 教师对象，如果不存在则返回null
     */
    suspend fun getTeacherByName(name: String): Teacher? {
        return courseDao.getTeacherByName(name)
    }

    /**
     * 添加老师
     */
    suspend fun addTeacher(teacher: Teacher): Long {
        return courseDao.insertTeacher(teacher)
    }

    /**
     * 更新老师
     */
    suspend fun updateTeacher(teacher: Teacher) {
        courseDao.updateTeacher(teacher)
    }

    /**
     * 删除老师
     */
    suspend fun deleteTeacher(teacher: Teacher) {
        courseDao.deleteTeacher(teacher)
    }

    /**
     * 获取所有节次时间
     */
    suspend fun getAllPeriods(): List<Period> {
        return courseDao.getAllPeriods().first()
    }

    /**
     * 添加节次时间
     */
    suspend fun addPeriod(period: Period): Long {
        return courseDao.insertPeriod(period)
    }

    /**
     * 更新节次时间
     */
    suspend fun updatePeriod(period: Period) {
        courseDao.updatePeriod(period)
    }

    /**
     * 删除节次时间
     */
    suspend fun deletePeriod(period: Period) {
        courseDao.deletePeriod(period)
    }

    /**
     * 删除指定时间表的所有课程
     */
    suspend fun deleteCoursesByTimetable(timetableId: Long) {
        courseDao.deleteSchedulesByTimetable(timetableId)
        courseDao.deleteCoursesByTimetable(timetableId)
    }

    /**
     * 重置指定时间表（删除所有相关数据）
     */
    suspend fun resetTimetable(timetableId: Long) {
        deleteCoursesByTimetable(timetableId)
    }

    /**
     * 获取所有调课记录
     */
    suspend fun loadAdjustments(): List<Adjust> {
        return courseDao.getAllAdjustments().first()
    }

    /**
     * 添加调课记录
     */
    suspend fun addAdjustment(adjustment: Adjust): Long {
        return courseDao.insertAdjustment(adjustment)
    }

    /**
     * 更新调课记录
     */
    suspend fun updateAdjustment(adjustment: Adjust) {
        courseDao.updateAdjustment(adjustment)
    }

    /**
     * 删除调课记录
     */
    suspend fun deleteAdjustment(adjustment: Adjust) {
        courseDao.deleteAdjustment(adjustment)
    }
    
    /**
     * 保存调课记录（使用数据库而不是SharedPreferences）
     */
    suspend fun saveAdjustments(adjustments: List<Adjust>) {
        // 清除现有数据
        courseDao.deleteAllAdjustments()
        // 插入新数据
        for (adjustment in adjustments) {
            courseDao.insertAdjustment(adjustment)
        }
    }
    
    /**
     * 添加示例数据（仅用于调试模式）
     */
    suspend fun addSampleData(): Long {
        // 使用默认课表并添加一些测试课程
        val timetableId = createDefaultTimetable()
        
        // 添加测试课程
        val course = Course(
            name = "高等数学",
            type = "必修",
            credit = 4.0,
            examTime = "2024-01-10 09:00",
            note = "期末考试",
            timetableId = timetableId
        )
        val courseId = courseDao.insertCourse(course)
        
        // 添加测试老师
        val teacher = Teacher(name = "张教授")
        val teacherId = courseDao.insertTeacher(teacher)
        
        // 添加测试地点
        val location = Location(
            campus = "主校区",
            building = "A栋",
            classroom = "A101"
        )
        val locationId = courseDao.insertLocation(location)
        
        // 添加测试时间安排
        val schedule = Schedule(
            courseId = courseId,
            weeks = setOf(1, 2, 3, 4, 5),
            dayOfWeek = 1, // 星期一
            startPeriod = 1,
            endPeriod = 2,
            locationId = locationId,
            teacherId = teacherId
        )
        courseDao.insertSchedule(schedule)
        
        return timetableId
    }
    
    /**
     * 初始化默认的节次时间表
     */
    suspend fun initializeDefaultPeriods() {
        // 检查是否已存在节次时间表数据
        val existingPeriods = courseDao.getAllPeriods().first()
        if (existingPeriods.isNotEmpty()) return // 如果已有数据，则不初始化

        // 使用Period类中的默认节次时间表
        val defaultPeriods = Period.createDefaultPeriods()
        
        // 添加默认节次时间表到数据库
        for (period in defaultPeriods) {
            courseDao.insertPeriod(period)
        }
    }
    
    /**
     * 创建默认课表
     */
    suspend fun createDefaultTimetable(): Long {
        val existingTimetables = courseDao.getAllTimetables().first()
        if (existingTimetables.isNotEmpty()) {
            return existingTimetables[0].id
        }
        val defaultTimetable = Timetable(
            name = "默认课表",
            semester = "2023-2024学年",
            classId = "默认班级",
            note = "系统自动创建的默认课表"
        )
        return courseDao.insertTimetable(defaultTimetable)
    }
}