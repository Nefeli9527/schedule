package top.nefeli.schedule.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import top.nefeli.schedule.data.SettingsRepository
import top.nefeli.schedule.model.Settings

/**
 * 设置 ViewModel 类
 * 负责管理应用设置相关的数据和业务逻辑
 * 使用 MVVM 架构模式，通过 StateFlow 向 UI 层暴露数据状态
 *
 * @param repository 设置数据仓库，用于访问设置数据
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
    }

    /**
     * 应用设置状态流
     * 使用 stateIn 将 Flow 转换为 StateFlow，以便在 UI 中使用
     */
    val settings: StateFlow<Settings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Settings()
    )

    /**
     * 更新应用设置
     *
     * @param newSettings 新的设置
     */
    fun updateSettings(newSettings: Settings) {
        viewModelScope.launch {
            try {
                repository.updateSettings(newSettings)
            } catch (e: Exception) {
                Log.e(TAG, "更新设置失败", e)
            }
        }
    }
}

/**
 * SettingsViewModel 的工厂类
 * 用于创建 SettingsViewModel 实例
 *
 * @param repository 设置数据仓库
 */
class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}