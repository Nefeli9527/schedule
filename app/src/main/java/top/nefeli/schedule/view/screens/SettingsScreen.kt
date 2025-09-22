package top.nefeli.schedule.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Settings
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModelFactory: SettingsViewModelFactory,
    onBack: () -> Unit,
    navigateToTimetableSettings: () -> Unit = {}
) {
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val currentSettings by settingsViewModel.settings.collectAsStateWithLifecycle()
    var numberOfPeriods by remember { mutableStateOf(currentSettings.numberOfPeriods) }
    var showWeekends by remember { mutableStateOf(currentSettings.showWeekends) }
    var semesterStartDate by remember { mutableStateOf(currentSettings.semesterStartDate) }
    var totalWeeks by remember { mutableStateOf(currentSettings.totalWeeks) }
    var enableWeekNavigation by remember { mutableStateOf(currentSettings.enableWeekNavigation) }
    var doubleBackToExit by remember { mutableStateOf(currentSettings.doubleBackToExit) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentSettings.semesterStartDate.toEpochDay() * 86400000
    )
    
    // 自动保存设置
    LaunchedEffect(numberOfPeriods, showWeekends, semesterStartDate, totalWeeks, enableWeekNavigation, doubleBackToExit) {
        settingsViewModel.updateSettings(
            Settings(numberOfPeriods, showWeekends, semesterStartDate, totalWeeks, enableWeekNavigation, doubleBackToExit)
        )
    }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.settings_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cancel),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            }
        ) { padding -> 
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.periods_per_day)) },
                    supportingContent = { Text(stringResource(R.string.periods_per_day_desc)) },
                    trailingContent = {
                        OutlinedTextField(
                            value = numberOfPeriods.toString(),
                            onValueChange = {
                                val value = it.toIntOrNull()
                                if (value != null && value > 0) {
                                    numberOfPeriods = value
                                }
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
                            onCheckedChange = { showWeekends = it }
                        )
                    }
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.total_weeks)) },
                    supportingContent = { Text(stringResource(R.string.total_weeks_desc)) },
                    trailingContent = {
                        OutlinedTextField(
                            value = totalWeeks.toString(),
                            onValueChange = {
                                val value = it.toIntOrNull()
                                if (value != null && value > 0 && value <= 50) {
                                    totalWeeks = value
                                }
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
                            onCheckedChange = { enableWeekNavigation = it }
                        )
                    }
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.double_back_to_exit)) },
                    supportingContent = { Text(stringResource(R.string.double_back_to_exit_desc)) },
                    trailingContent = {
                        Switch(
                            checked = doubleBackToExit,
                            onCheckedChange = { doubleBackToExit = it }
                        )
                    }
                )
                
                ListItem(
                    headlineContent = { Text(stringResource(R.string.semester_start_date)) },
                    supportingContent = { Text(stringResource(R.string.semester_start_date_desc, semesterStartDate.toString())) },
                    trailingContent = {
                        Button(onClick = { showDatePicker = true }) {
                            Text(stringResource(R.string.select))
                        }
                    }
                )
                
                // 添加时间表设置入口
                ListItem(
                    headlineContent = { Text(stringResource(R.string.timetable_settings)) },
                    supportingContent = { Text(stringResource(R.string.quick_setup_description)) },
                    trailingContent = {
                        Button(onClick = { navigateToTimetableSettings() }) {
                            Text(stringResource(id = R.string.settings))
                        }
                    }
                )
                
                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val daysSinceEpoch = millis / 86400000
                                        semesterStartDate = LocalDate.ofEpochDay(daysSinceEpoch)
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text(stringResource(R.string.ok))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
            }
        }
    }
}