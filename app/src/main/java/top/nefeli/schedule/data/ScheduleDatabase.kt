package top.nefeli.schedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import top.nefeli.schedule.model.Adjust
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.Location
import top.nefeli.schedule.model.Schedule
import top.nefeli.schedule.model.Teacher
import top.nefeli.schedule.model.Timetable
import top.nefeli.schedule.model.TimetableSchedule

@Database(
    entities = [Timetable::class, Course::class, Schedule::class, Location::class, Teacher::class, TimetableSchedule::class, Adjust::class],
    version = 1, // 更新版本号为2
    exportSchema = false
)
@TypeConverters(Converters::class, Course.Converters::class, Schedule.Converters::class)
abstract class ScheduleDatabase : RoomDatabase() {
    
    abstract fun courseDao(): CourseDao
    
    companion object {
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null
        
        fun getDatabase(context: Context): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "schedule_database"
                )
//                .fallbackToDestructiveMigration() // 允许破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}