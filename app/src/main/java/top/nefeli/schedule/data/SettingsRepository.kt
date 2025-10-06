package top.nefeli.schedule.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import top.nefeli.schedule.model.Settings
import java.time.LocalDate

/**
 * 设置数据仓库类
 * 负责应用设置的持久化存储和读取
 */
class SettingsRepository(context: Context) {
    // 用于存储设置数据的SharedPreferences
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("schedule_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<Settings> = _settings.asStateFlow()

    /**
     * 从SharedPreferences加载应用设置
     *
     * @return Settings对象，包含所有应用设置
     */
    fun loadSettings(): Settings {
        // 读取节次设置，默认为9节
        val numberOfPeriods = sharedPreferences.getInt("number_of_periods", 9)
        // 读取是否显示周末设置，默认为false
        val showWeekends = sharedPreferences.getBoolean("show_weekends", false)
        // 读取学期开始日期，如果没有保存则使用默认值
        val startDateString = sharedPreferences.getString("semester_start_date", null)
        // 如果没有保存开学日期，则使用默认日期
        val semesterStartDate = if (startDateString != null) {
            LocalDate.parse(startDateString)
        } else {
            LocalDate.now()
        }
        // 读取总周数设置，默认为22周
        val totalWeeks = sharedPreferences.getInt("total_weeks", 22)
        // 读取是否启用周次导航设置，默认为true
        val enableWeekNavigation = sharedPreferences.getBoolean("enable_week_navigation", true)
        // 读取双击返回退出设置，默认为true
        val doubleBackToExit = sharedPreferences.getBoolean("double_back_to_exit", true)
        // 读取勿扰模式设置，默认为false
        val doNotDisturbEnabled = sharedPreferences.getBoolean("do_not_disturb_enabled", false)
        // 读取通知设置，默认为true
        val notificationEnabled = sharedPreferences.getBoolean("notification_enabled", true)
        // 读取主题设置，默认为auto
        val theme = sharedPreferences.getString("theme", "auto") ?: "auto"
        
        // 创建并返回Settings对象
        return Settings(
            numberOfPeriods = numberOfPeriods,
            showWeekends = showWeekends,
            semesterStartDate = semesterStartDate,
            totalWeeks = totalWeeks,
            enableWeekNavigation = enableWeekNavigation,
            doubleBackToExit = doubleBackToExit,
            doNotDisturbEnabled = doNotDisturbEnabled,
            notificationEnabled = notificationEnabled,
            theme = theme
        )
    }

    /**
     * 将应用设置保存到SharedPreferences
     *
     * @param settings 要保存的设置
     */
    fun saveSettings(settings: Settings) {
        sharedPreferences.edit {
            // 保存节次设置
            putInt("number_of_periods", settings.numberOfPeriods)
            // 保存是否显示周末设置
            putBoolean("show_weekends", settings.showWeekends)
            // 保存学期开始日期
            putString("semester_start_date", settings.semesterStartDate.toString())
            // 保存总周数设置
            putInt("total_weeks", settings.totalWeeks)
            // 保存是否启用周次导航设置
            putBoolean("enable_week_navigation", settings.enableWeekNavigation)
            // 保存双击返回退出设置
            putBoolean("double_back_to_exit", settings.doubleBackToExit)
            // 保存勿扰模式设置
            putBoolean("do_not_disturb_enabled", settings.doNotDisturbEnabled)
            // 保存通知设置
            putBoolean("notification_enabled", settings.notificationEnabled)
            // 保存主题设置
            putString("theme", settings.theme)
        }

        // 更新内存中的设置
        _settings.update { settings }
    }

    /**
     * 获取当前设置
     */
    fun getSettings(): Settings {
        return _settings.value
    }

    /**
     * 更新设置
     */
    fun updateSettings(settings: Settings) {
        saveSettings(settings)
    }
}