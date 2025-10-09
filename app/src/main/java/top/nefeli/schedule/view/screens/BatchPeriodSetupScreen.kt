package top.nefeli.schedule.view.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.nefeli.schedule.R
import top.nefeli.schedule.model.Period
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchPeriodSetupScreen(
    scheduleViewModelFactory: ScheduleViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    onBack: () -> Unit,
) {
    val scheduleViewModel: ScheduleViewModel = viewModel(factory = scheduleViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)

    // 上午设置
    var morningCount: Int by remember { mutableIntStateOf(4) }
    var morningStartTime: LocalTime by remember { mutableStateOf(LocalTime.of(8, 0)) }

    // 下午设置
    var afternoonCount: Int by remember { mutableIntStateOf(4) }
    var afternoonStartTime: LocalTime by remember { mutableStateOf(LocalTime.of(14, 0)) }

    // 晚上设置
    var eveningCount: Int by remember { mutableIntStateOf(1) }
    var eveningStartTime: LocalTime by remember { mutableStateOf(LocalTime.of(19, 0)) }

    // 通用设置
    var classDuration: Int by remember { mutableIntStateOf(45) } // 课程时长（分钟）
    var breakDuration: Int by remember { mutableIntStateOf(10) } // 课间时长（分钟）
    var largeBreakDuration: Int by remember { mutableIntStateOf(20) } // 大课间时长（分钟）
    var largeBreakPositions: String by remember { mutableStateOf("2") } // 大课间位置（可以多个，用逗号分隔）

    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showAfternoonTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }

    if (showMorningTimePicker) {
        TimePickerDialogBatch(
            onDismiss = { showMorningTimePicker = false },
            onConfirm = { selectedTime ->
                morningStartTime = selectedTime
                showMorningTimePicker = false
            },
            initialTime = morningStartTime
        )
    }

    if (showAfternoonTimePicker) {
        TimePickerDialogBatch(
            onDismiss = { showAfternoonTimePicker = false },
            onConfirm = { selectedTime ->
                afternoonStartTime = selectedTime
                showAfternoonTimePicker = false
            },
            initialTime = afternoonStartTime
        )
    }

    if (showEveningTimePicker) {
        TimePickerDialogBatch(
            onDismiss = { showEveningTimePicker = false },
            onConfirm = { selectedTime ->
                eveningStartTime = selectedTime
                showEveningTimePicker = false
            },
            initialTime = eveningStartTime
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.batch_period_setup)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val periods = generatePeriodsWithMultipleBreaks(
                        morningCount,
                        morningStartTime,
                        afternoonCount,
                        afternoonStartTime,
                        eveningCount,
                        eveningStartTime,
                        classDuration,
                        breakDuration,
                        largeBreakDuration,
                        largeBreakPositions
                    )
                    // 删除所有现有时段
                    scheduleViewModel.period.value.forEach { period ->
                        scheduleViewModel.deletePeriod(period)
                    }
                    // 添加新时段
                    periods.forEach { period ->
                        scheduleViewModel.addPeriod(period)
                    }
                    // 更新设置中的课程节数
                    val totalPeriods = morningCount + afternoonCount + eveningCount
                    settingsViewModel.updateSettings(
                        settingsViewModel.settings.value.copy(numberOfPeriods = totalPeriods)
                    )
                    // 返回上一页
                    onBack()
                },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text(stringResource(R.string.apply)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.batch_period_setup_desc),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 上午设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.morning_settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PeriodCountField(
                        label = stringResource(R.string.morning_class_count),
                        value = morningCount,
                        onValueChange = { morningCount = it }
                    )

                    TimeField(
                        label = stringResource(R.string.morning_start_time),
                        time = morningStartTime,
                        onClick = { showMorningTimePicker = true }
                    )
                }
            }

            // 下午设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.afternoon_settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PeriodCountField(
                        label = stringResource(R.string.afternoon_class_count),
                        value = afternoonCount,
                        onValueChange = { afternoonCount = it }
                    )

                    TimeField(
                        label = stringResource(R.string.afternoon_start_time),
                        time = afternoonStartTime,
                        onClick = { showAfternoonTimePicker = true }
                    )
                }
            }

            // 晚上设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.evening_settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PeriodCountField(
                        label = stringResource(R.string.evening_class_count),
                        value = eveningCount,
                        onValueChange = { eveningCount = it }
                    )

                    TimeField(
                        label = stringResource(R.string.evening_start_time),
                        time = eveningStartTime,
                        onClick = { showEveningTimePicker = true }
                    )
                }
            }

            // 通用设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.general_settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DurationField(
                        label = stringResource(R.string.class_duration),
                        value = classDuration,
                        onValueChange = { classDuration = it }
                    )

                    DurationField(
                        label = stringResource(R.string.break_duration),
                        value = breakDuration,
                        onValueChange = { breakDuration = it }
                    )

                    DurationField(
                        label = stringResource(R.string.large_break_duration),
                        value = largeBreakDuration,
                        onValueChange = { largeBreakDuration = it }
                    )

                    BreakPositionsField(
                        label = stringResource(R.string.large_break_position),
                        value = largeBreakPositions,
                        onValueChange = { largeBreakPositions = it }
                    )
                }
            }
        }
    }
}

@Composable
fun PeriodCountField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { onValueChange(it) }
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.periods_unit))
        }
    }
}

@Composable
fun DurationField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { onValueChange(it) }
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.minutes_unit))
        }
    }
}

@Composable
fun BreakPositionsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.break_periods_hint)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.break_periods_example),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimeField(
    label: String,
    time: LocalTime,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onClick) {
            Text(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(time)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogBatch(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    initialTime: LocalTime,
) {
    var selectedTime by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val state = rememberTimePickerState(
                    initialHour = selectedTime.hour,
                    initialMinute = selectedTime.minute,
                    is24Hour = true
                )
                TimePicker(
                    state = state,
                    modifier = Modifier
                )

                // 监听时间变化并更新selectedTime
                LaunchedEffect(state.hour, state.minute) {
                    selectedTime = LocalTime.of(state.hour, state.minute)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedTime)
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

fun generatePeriodsWithMultipleBreaks(
    morningCount: Int,
    morningStartTime: LocalTime,
    afternoonCount: Int,
    afternoonStartTime: LocalTime,
    eveningCount: Int,
    eveningStartTime: LocalTime,
    classDuration: Int,
    breakDuration: Int,
    largeBreakDuration: Int,
    largeBreakPositions: String,
): List<Period> {
    val periods = mutableListOf<Period>()
    var sortOrder = 1

    // 解析大课间位置
    val breakPositions = largeBreakPositions.split(",")
        .mapNotNull { it.trim().toIntOrNull() }
        .toSet()

    // 生成上午时段
    var currentTime = morningStartTime
    for (i in 1..morningCount) {
        val endTime = currentTime.plusMinutes(classDuration.toLong())
        periods.add(
            Period(
                name = "第${i}节课",
                startTime = currentTime,
                endTime = endTime,
                periodType = "上午",
                sortOrder = sortOrder++
            )
        )
        // 计算下一节课开始时间
        currentTime = endTime.plusMinutes(
            if (i in breakPositions) largeBreakDuration.toLong()
            else breakDuration.toLong()
        )
    }

    // 生成下午时段
    currentTime = afternoonStartTime
    for (i in 1..afternoonCount) {
        val endTime = currentTime.plusMinutes(classDuration.toLong())
        periods.add(
            Period(
                name = "第${morningCount + i}节课",
                startTime = currentTime,
                endTime = endTime,
                periodType = "下午",
                sortOrder = sortOrder++
            )
        )
        // 计算下一节课开始时间
        currentTime = endTime.plusMinutes(
            if ((morningCount + i) in breakPositions) largeBreakDuration.toLong()
            else breakDuration.toLong()
        )
    }

    // 生成晚上时段
    currentTime = eveningStartTime
    val eveningStartIndex = morningCount + afternoonCount
    for (i in 1..eveningCount) {
        val endTime = currentTime.plusMinutes(classDuration.toLong())
        periods.add(
            Period(
                name = "第${eveningStartIndex + i}节课",
                startTime = currentTime,
                endTime = endTime,
                periodType = "晚上",
                sortOrder = sortOrder++
            )
        )
        // 计算下一节课开始时间（晚上通常没有大课间）
        currentTime = endTime.plusMinutes(breakDuration.toLong())
    }

    return periods
}