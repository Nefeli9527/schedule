package top.nefeli.schedule.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class Settings constructor(
    val numberOfPeriods: Int = 9,
    val showWeekends: Boolean = false,
    val semesterStartDate: LocalDate = LocalDate.now().minusYears(1), // 默认使用一年前的日期而不是今天
    val totalWeeks: Int = 22,
    val enableWeekNavigation: Boolean = true, // 启用周次导航功能
    val doubleBackToExit: Boolean = true // 双击返回退出应用
)