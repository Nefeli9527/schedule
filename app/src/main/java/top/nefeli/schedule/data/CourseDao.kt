package top.nefeli.schedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.nefeli.schedule.model.Adjust
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.Timetable
import top.nefeli.schedule.model.TimetableSchedule

@Dao
interface CourseDao {
    // Timetable相关操作
    /**
     * 获取所有时间表
     *
     * @return 包含所有时间表的Flow列表
     */
    @Query("SELECT * FROM timetables")
    fun getAllTimetables(): Flow<List<Timetable>>

    /**
     * 根据ID获取时间表
     *
     * @param id 时间表ID
     * @return 时间表对象，如果不存在则返回null
     */
    @Query("SELECT * FROM timetables WHERE id = :id")
    suspend fun getTimetableById(id: Long): Timetable?

    /**
     * 插入时间表
     *
     * @param timetable 要插入的时间表对象
     * @return 插入后的时间表ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(timetable: Timetable): Long

    /**
     * 更新时间表
     *
     * @param timetable 要更新的时间表对象
     */
    @Update
    suspend fun updateTimetable(timetable: Timetable)

    /**
     * 删除时间表
     *
     * @param timetable 要删除的时间表对象
     */
    @Delete
    suspend fun deleteTimetable(timetable: Timetable)

    // Course相关操作
    /**
     * 获取所有课程
     *
     * @return 包含所有课程的Flow列表
     */
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    /**
     * 根据ID获取课程
     *
     * @param id 课程ID
     * @return 课程对象，如果不存在则返回null
     */
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?

    /**
     * 根据时间表ID获取课程列表
     *
     * @param timetableId 时间表ID
     * @return 包含指定时间表中所有课程的Flow列表
     */
    @Query("SELECT * FROM courses WHERE timetableId = :timetableId")
    fun getCoursesByTimetable(timetableId: Long): Flow<List<Course>>

    /**
     * 根据课表ID和课程名称查询课程
     *
     * @param timetableId 课表ID
     * @param name 课程名称
     * @return 符合条件的课程列表
     */
    @Query("SELECT * FROM courses WHERE timetableId = :timetableId AND name = :name")
    suspend fun getCourseByTimetableAndName(timetableId: Long, name: String): List<Course>

    /**
     * 插入课程
     *
     * @param course 要插入的课程对象
     * @return 插入后的课程ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    /**
     * 批量插入课程
     *
     * @param courses 要插入的课程列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    /**
     * 更新课程
     *
     * @param course 要更新的课程对象
     */
    @Update
    suspend fun updateCourse(course: Course)

    /**
     * 删除课程
     *
     * @param course 要删除的课程对象
     */
    @Delete
    suspend fun deleteCourse(course: Course)

    /**
     * 删除指定时间表的所有课程
     */
    @Query("DELETE FROM courses WHERE timetableId = :timetableId")
    suspend fun deleteCoursesByTimetable(timetableId: Long)

    /**
     * 删除指定时间表的所有课程安排
     */
    @Query("DELETE FROM schedules WHERE courseId IN (SELECT id FROM courses WHERE timetableId = :timetableId)")
    suspend fun deleteSchedulesByTimetable(timetableId: Long)

    // Schedule相关操作
    /**
     * 获取所有课程安排
     *
     * @return 包含所有课程安排的Flow列表
     */
    @Query("SELECT * FROM schedules")
    fun getAllSchedules(): Flow<List<Schedule>>

    /**
     * 根据ID获取课程安排
     *
     * @param id 课程安排ID
     * @return 课程安排对象，如果不存在则返回null
     */
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): Schedule?

    /**
     * 根据课程ID获取课程安排列表
     *
     * @param courseId 课程ID
     * @return 包含指定课程中所有课程安排的Flow列表
     */
    @Query("SELECT * FROM schedules WHERE courseId = :courseId")
    fun getSchedulesByCourse(courseId: Long): Flow<List<Schedule>>

    /**
     * 根据时间表ID获取课程安排列表
     *
     * @param timetableId 时间表ID
     * @return 包含指定时间表中所有课程安排的Flow列表
     */
    @Query("SELECT * FROM schedules WHERE courseId IN (SELECT id FROM courses WHERE timetableId = :timetableId)")
    fun getSchedulesByTimetable(timetableId: Long): Flow<List<Schedule>>

    /**
     * 插入课程安排
     *
     * @param schedule 要插入的课程安排对象
     * @return 插入后的课程安排ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: Schedule): Long

    /**
     * 更新课程安排
     *
     * @param schedule 要更新的课程安排对象
     */
    @Update
    suspend fun updateSchedule(schedule: Schedule)

    /**
     * 删除课程安排
     *
     * @param schedule 要删除的课程安排对象
     */
    @Delete
    suspend fun deleteSchedule(schedule: Schedule)

    // Location相关操作
    /**
     * 获取所有地点
     *
     * @return 包含所有地点的Flow列表
     */
    @Query("SELECT * FROM locations")
    fun getAllLocations(): Flow<List<Location>>

    /**
     * 根据地点获取id
     *
     * @param id 地点ID
     * @return 地点对象，如果不存在则返回null
     */
    @Query("SELECT id FROM locations WHERE campus = :campus AND building = :building AND classroom = :classroom")
    suspend fun getIdByLocation(campus: String?, building: String?, classroom: String): Long?

    /**
     * 根据ID获取地点
     *
     * @param id 地点ID
     * @return 地点对象，如果不存在则返回null
     */
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): Location?

    /**
     * 根据详细信息获取地点
     *
     * @param campus 校区
     * @param building 教学楼
     * @param classroom 教室
     * @return 地点对象，如果不存在则返回null
     */
    @Query("SELECT * FROM locations WHERE campus = :campus AND building = :building AND classroom = :classroom LIMIT 1")
    suspend fun getLocationByDetails(campus: String?, building: String?, classroom: String): Location?

    /**
     * 插入地点
     *
     * @param location 要插入的地点对象
     * @return 插入后的地点ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    /**
     * 更新地点
     *
     * @param location 要更新的地点对象
     */
    @Update
    suspend fun updateLocation(location: Location)

    /**
     * 删除地点
     *
     * @param location 要删除的地点对象
     */
    @Delete
    suspend fun deleteLocation(location: Location)

    // Teacher相关操作
    /**
     * 获取所有教师
     *
     * @return 包含所有教师的Flow列表
     */
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<Teacher>>

    /**
     * 根据ID获取教师
     *
     * @param id 教师ID
     * @return 教师对象，如果不存在则返回null
     */
    @Query("SELECT id FROM teachers WHERE name = :name")
    suspend fun getIdByTeacher(name: String): Long?

    /**
     * 根据ID获取教师
     *
     * @param id 教师ID
     * @return 教师对象，如果不存在则返回null
     */
    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: Long): Teacher?

    /**
     * 根据姓名获取教师
     *
     * @param name 教师姓名
     * @return 教师对象，如果不存在则返回null
     */
    @Query("SELECT * FROM teachers WHERE name = :name LIMIT 1")
    suspend fun getTeacherByName(name: String): Teacher?

    /**
     * 插入教师
     *
     * @param teacher 要插入的教师对象
     * @return 插入后的教师ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    /**
     * 更新教师
     *
     * @param teacher 要更新的教师对象
     */
    @Update
    suspend fun updateTeacher(teacher: Teacher)

    /**
     * 删除教师
     *
     * @param teacher 要删除的教师对象
     */
    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    // TimetableSchedule相关操作
    /**
     * 获取所有时间表课程安排，按排序顺序排列
     *
     * @return 包含所有时间表课程安排的Flow列表
     */
    @Query("SELECT * FROM timetable_schedules ORDER BY sortOrder")
    fun getAllTimetableSchedules(): Flow<List<TimetableSchedule>>

    /**
     * 根据ID获取时间表课程安排
     *
     * @param id 时间表课程安排ID
     * @return 时间表课程安排对象，如果不存在则返回null
     */
    @Query("SELECT * FROM timetable_schedules WHERE id = :id")
    suspend fun getTimetableScheduleById(id: Long): TimetableSchedule?

    /**
     * 插入时间表课程安排
     *
     * @param schedule 要插入的时间表课程安排对象
     * @return 插入后的时间表课程安排ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSchedule(schedule: TimetableSchedule): Long

    /**
     * 更新时间表课程安排
     *
     * @param schedule 要更新的时间表课程安排对象
     */
    @Update
    suspend fun updateTimetableSchedule(schedule: TimetableSchedule)

    /**
     * 删除时间表课程安排
     *
     * @param schedule 要删除的时间表课程安排对象
     */
    @Delete
    suspend fun deleteTimetableSchedule(schedule: TimetableSchedule)

    // Adjust相关操作
    /**
     * 获取所有调整项
     *
     * @return 包含所有调整项的Flow列表
     */
    @Query("SELECT * FROM adjustments")
    fun getAllAdjustments(): Flow<List<Adjust>>

    /**
     * 根据ID获取调整项
     *
     * @param id 调整项ID
     * @return 调整项对象，如果不存在则返回null
     */
    @Query("SELECT * FROM adjustments WHERE id = :id")
    suspend fun getAdjustmentById(id: Long): Adjust?

    /**
     * 插入调整项
     *
     * @param adjustment 要插入的调整项对象
     * @return 插入后的调整项ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: Adjust): Long

    /**
     * 更新调整项
     *
     * @param adjustment 要更新的调整项对象
     */
    @Update
    suspend fun updateAdjustment(adjustment: Adjust)

    /**
     * 删除调整项
     *
     * @param adjustment 要删除的调整项对象
     */
    @Delete
    suspend fun deleteAdjustment(adjustment: Adjust)

    // 清空所有数据的操作
    /**
     * 删除所有时间表数据
     */
    @Query("DELETE FROM timetables")
    suspend fun deleteAllTimetables()

    /**
     * 删除所有课程数据
     */
    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    /**
     * 删除所有课程安排数据
     */
    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules()

    /**
     * 删除所有地点数据
     */
    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()

    /**
     * 删除所有教师数据
     */
    @Query("DELETE FROM teachers")
    suspend fun deleteAllTeachers()

    /**
     * 删除所有时间表课程安排数据
     */
    @Query("DELETE FROM timetable_schedules")
    suspend fun deleteAllTimetableSchedules()

    /**
     * 删除所有调整项数据
     */
    @Query("DELETE FROM adjustments")
    suspend fun deleteAllAdjustments()
}