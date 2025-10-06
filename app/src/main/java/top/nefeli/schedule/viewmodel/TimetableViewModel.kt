package top.nefeli.schedule.viewmodel

// import top.nefeli.schedule.model.TimetableConfig
// import top.nefeli.schedule.model.PeriodTime
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalTime

/**
 * 时间表 ViewModel
 * 负责管理时间表配置的业务逻辑
 */
class TimetableViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()
    
    init {
        loadTimetableConfig()
    }
    
    /**
     * 加载时间表配置
     */
    private fun loadTimetableConfig() {
        /* viewModelScope.launch {
            try {
                val config = timetableRepository.loadTimetableConfig()
                _uiState.value = _uiState.value.copy(
                    timetableConfig = config,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "加载时间表配置失败: ${e.message}"
                )
            }
        } */
    }
    
    /**
     * 更新子表的时间安排
     */
    fun updateSubTimetablePeriods(subTimetableIndex: Int, periodTimes: List</* PeriodTime */ Any>) {
        /* viewModelScope.launch {
            try {
                val updatedConfig = timetableRepository.updateSubTimetablePeriods(
                    _uiState.value.timetableConfig,
                    subTimetableIndex,
                    periodTimes
                )
                _uiState.value = _uiState.value.copy(
                    timetableConfig = updatedConfig
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新时间安排失败: ${e.message}"
                )
            }
        } */
    }
    
    /**
     * 使用快捷设置更新子表的时间安排
     */
    fun quickUpdateSubTimetablePeriods(
        subTimetableIndex: Int,
        morningStart: LocalTime,
        afternoonStart: LocalTime,
        eveningStart: LocalTime,
        periodDuration: Int,
        breakDuration: Int,
        longBreakDuration: Int,
        breakPeriods: Set<Int>
    ) {
        /* viewModelScope.launch {
            try {
                val updatedConfig = timetableRepository.quickUpdateSubTimetablePeriods(
                    _uiState.value.timetableConfig,
                    subTimetableIndex,
                    morningStart,
                    afternoonStart,
                    eveningStart,
                    periodDuration,
                    breakDuration,
                    longBreakDuration,
                    breakPeriods
                )
                _uiState.value = _uiState.value.copy(
                    timetableConfig = updatedConfig
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "快捷设置时间安排失败: ${e.message}"
                )
            }
        } */
    }
    
    /**
     * 设置当前激活的子表
     */
    fun setActiveSubTimetable(index: Int) {
        /* viewModelScope.launch {
            try {
                val updatedConfig = timetableRepository.setActiveSubTimetable(
                    _uiState.value.timetableConfig,
                    index
                )
                _uiState.value = _uiState.value.copy(
                    timetableConfig = updatedConfig
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "切换子表失败: ${e.message}"
                )
            }
        } */
    }
    
    /**
     * 更新切换日期
     */
    fun updateSwitchDate(switchDate: LocalDate) {
        /* viewModelScope.launch {
            try {
                val updatedConfig = timetableRepository.updateSwitchDate(
                    _uiState.value.timetableConfig,
                    switchDate
                )
                _uiState.value = _uiState.value.copy(
                    timetableConfig = updatedConfig
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "更新切换日期失败: ${e.message}"
                )
            }
        } */
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * 时间表界面状态
 */
data class TimetableUiState(
    // val timetableConfig: TimetableConfig = TimetableConfig("默认时间表"),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)