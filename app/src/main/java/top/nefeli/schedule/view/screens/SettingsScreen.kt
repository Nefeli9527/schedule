package top.nefeli.schedule.view.screens

import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Settings
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModelFactory: SettingsViewModelFactory,
    onBack: () -> Unit,
    navigateToTimetableSettings: () -> Unit,
    navigateToPeriodManagement: () -> Unit,
) {
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val settings by settingsViewModel.settings.collectAsState()
    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()

    var numberOfPeriods by remember { mutableStateOf(settings.numberOfPeriods.toString()) }
    var showWeekends by remember { mutableStateOf(settings.showWeekends) }
    var totalWeeks by remember { mutableStateOf(settings.totalWeeks.toString()) }
    var enableWeekNavigation by remember { mutableStateOf(settings.enableWeekNavigation) }
    var doubleBackToExit by remember { mutableStateOf(settings.doubleBackToExit) }
    var doNotDisturbEnabled by remember { mutableStateOf(settings.doNotDisturbEnabled) }
    var semesterStartDate by remember { mutableStateOf(settings.semesterStartDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    // 监听设置变化并更新本地状态
    LaunchedEffect(settings) {
        numberOfPeriods = settings.numberOfPeriods.toString()
        showWeekends = settings.showWeekends
        totalWeeks = settings.totalWeeks.toString()
        enableWeekNavigation = settings.enableWeekNavigation
        doubleBackToExit = settings.doubleBackToExit
        doNotDisturbEnabled = settings.doNotDisturbEnabled
        semesterStartDate = settings.semesterStartDate
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                semesterStartDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePicker = false
                // 立即保存更改
                saveSettings(
                    settingsViewModel,
                    Settings(
                        numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                        showWeekends = showWeekends,
                        semesterStartDate = semesterStartDate,
                        totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                        enableWeekNavigation = enableWeekNavigation,
                        doubleBackToExit = doubleBackToExit,
                        doNotDisturbEnabled = doNotDisturbEnabled
                    )
                )
            },
            semesterStartDate.year,
            semesterStartDate.monthValue - 1,
            semesterStartDate.dayOfMonth
        ).show()
        showDatePicker = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues -> 
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 课表设置部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.periods_per_day)) },
                        supportingContent = { Text(stringResource(R.string.periods_per_day_desc)) },
                        trailingContent = {
                            OutlinedTextField(
                                value = numberOfPeriods,
                                onValueChange = {
                                    numberOfPeriods = it
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = it.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                },
                                modifier = Modifier.width(100.dp),
                                singleLine = true
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.show_weekends)) },
                        supportingContent = { Text(stringResource(R.string.show_weekends_desc)) },
                        trailingContent = {
                            Switch(
                                checked = showWeekends,
                                onCheckedChange = {
                                    showWeekends = it
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                }
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.total_weeks)) },
                        supportingContent = { Text(stringResource(R.string.total_weeks_desc)) },
                        trailingContent = {
                            OutlinedTextField(
                                value = totalWeeks,
                                onValueChange = {
                                    totalWeeks = it
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = it.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                },
                                modifier = Modifier.width(100.dp),
                                singleLine = true
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.enable_week_navigation)) },
                        supportingContent = { Text(stringResource(R.string.enable_week_navigation_desc)) },
                        trailingContent = {
                            Switch(
                                checked = enableWeekNavigation,
                                onCheckedChange = {
                                    enableWeekNavigation = it
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                }
                            )
                        }
                    )

                    ListItem(
                        headlineContent = { Text(stringResource(R.string.semester_start_date)) },
                        supportingContent = {
                            Text(
                                stringResource(
                                    R.string.semester_start_date_desc,
                                    semesterStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                )
                            )
                        },
                        trailingContent = {
                            Button(onClick = { showDatePicker = true }) {
                                Text(stringResource(R.string.change))
                            }
                        }
                    )
                }
            }

            // 应用设置部分
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.do_not_disturb_mode)) },
                        supportingContent = { Text(stringResource(R.string.do_not_disturb_mode_desc)) },
                        trailingContent = {
                            Switch(
                                checked = doNotDisturbEnabled,
                                onCheckedChange = { checked ->
                                    // 如果用户开启了勿扰模式，检查权限
                                    if (checked) {
                                        // 检查是否已有权限
                                        val notificationManager =
                                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        val hasDoNotDisturbPermission =
                                            notificationManager.isNotificationPolicyAccessGranted

                                        // 只有在没有权限时才跳转到设置页面
                                        if (!hasDoNotDisturbPermission) {
                                            val intent =
                                                Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                    }

                                    doNotDisturbEnabled = checked
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                }
                            )
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.double_back_to_exit)) },
                        supportingContent = { Text(stringResource(R.string.double_back_to_exit_desc)) },
                        trailingContent = {
                            Switch(
                                checked = doubleBackToExit,
                                onCheckedChange = {
                                    doubleBackToExit = it
                                    // 立即保存更改
                                    saveSettings(
                                        settingsViewModel,
                                        Settings(
                                            numberOfPeriods = numberOfPeriods.toIntOrNull() ?: 9,
                                            showWeekends = showWeekends,
                                            semesterStartDate = semesterStartDate,
                                            totalWeeks = totalWeeks.toIntOrNull() ?: 22,
                                            enableWeekNavigation = enableWeekNavigation,
                                            doubleBackToExit = doubleBackToExit,
                                            doNotDisturbEnabled = doNotDisturbEnabled
                                        )
                                    )
                                }
                            )
                        }
                    )
                }
            }

            // 时间表设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.timetable_settings)) },
                        supportingContent = { Text(stringResource(R.string.period_management_desc)) },
                        trailingContent = {
                            IconButton(onClick = navigateToPeriodManagement) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        },
                        modifier = Modifier.clickable { navigateToPeriodManagement() }
                    )
                }
            }
        }
    }
}

private fun saveSettings(settingsViewModel: SettingsViewModel, settings: Settings) {
    settingsViewModel.updateSettings(settings)
}