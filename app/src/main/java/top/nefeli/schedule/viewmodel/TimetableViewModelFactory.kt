package top.nefeli.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * TimetableViewModel 工厂类
 */
class TimetableViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimetableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimetableViewModel(/* timetableRepository */) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}