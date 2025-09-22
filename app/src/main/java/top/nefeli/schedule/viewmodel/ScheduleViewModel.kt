package top.nefeli.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.nefeli.schedule.model.Course
import top.nefeli.schedule.model.ScheduleRepository

class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {
    var scheduleData by mutableStateOf<Map<Pair<Int, Int>, Course>>(emptyMap())
        private set

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                repository.loadSchedule()
            }
            scheduleData = data
        }
    }

    fun addCourse(period: Int, day: Int, course: Course) {
        viewModelScope.launch {
            val updatedData = withContext(Dispatchers.IO) {
                repository.addCourse(period, day, course)
            }
            scheduleData = updatedData
        }
    }

    fun removeCourse(period: Int, day: Int) {
        viewModelScope.launch {
            val updatedData = withContext(Dispatchers.IO) {
                repository.removeCourse(period, day)
            }
            scheduleData = updatedData
        }
    }
}

class ScheduleViewModelFactory(private val repository: ScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
