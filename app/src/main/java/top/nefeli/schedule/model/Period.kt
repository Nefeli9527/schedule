package top.nefeli.schedule.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * 作息时间表实体类
 */
@Entity(tableName = "period")
data class Period(
    val name: String,              // 节次名称
    val startTime: LocalTime,      // 开始时间
    val endTime: LocalTime,        // 结束时间
    val periodType: String = "",   // 时段类型
    val sortOrder: Int = 0,        // 排序序号
    val note: String = "",         // 备注
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    companion object {
        /**
         * 创建默认的作息时间表
         */
        fun createDefaultPeriods(): List<Period> {
            return listOf(
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
                ),
                Period(
                    name = "第3节课",
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(10, 45),
                    periodType = "上午",
                    sortOrder = 3
                ),
                Period(
                    name = "第4节课",
                    startTime = LocalTime.of(10, 55),
                    endTime = LocalTime.of(11, 40),
                    periodType = "上午",
                    sortOrder = 4
                ),
                Period(
                    name = "第5节课",
                    startTime = LocalTime.of(14, 0),
                    endTime = LocalTime.of(14, 45),
                    periodType = "下午",
                    sortOrder = 5
                ),
                Period(
                    name = "第6节课",
                    startTime = LocalTime.of(14, 55),
                    endTime = LocalTime.of(15, 40),
                    periodType = "下午",
                    sortOrder = 6
                ),
                Period(
                    name = "第7节课",
                    startTime = LocalTime.of(16, 0),
                    endTime = LocalTime.of(16, 45),
                    periodType = "下午",
                    sortOrder = 7
                ),
                Period(
                    name = "第8节课",
                    startTime = LocalTime.of(16, 55),
                    endTime = LocalTime.of(17, 40),
                    periodType = "下午",
                    sortOrder = 8
                ),
                Period(
                    name = "第9节课",
                    startTime = LocalTime.of(19, 0),
                    endTime = LocalTime.of(19, 45),
                    periodType = "晚上",
                    sortOrder = 9
                )
            )
        }
    }
}