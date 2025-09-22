package top.nefeli.schedule.view.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import top.nefeli.schedule.R
import top.nefeli.schedule.viewmodel.TimetableViewModelFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 时间表设置界面
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableSettingsScreen(
    timetableViewModelFactory: TimetableViewModelFactory,
    onBack: () -> Unit
) {
    /* val timetableViewModel: TimetableViewModel = viewModel(factory = timetableViewModelFactory)
    val uiState by timetableViewModel.uiState.collectAsState()
    
    var showQuickSetupDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }*/
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timetable_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }/*,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickSetupDialog = true },
                icon = { Icon(Icons.Default.Build, contentDescription = null) },
                text = { Text(stringResource(R.string.quick_setup)) }
            )
        }*/
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "时间表设置功能正在开发中...")
            
            // 添加重置时间表按钮
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* 这里应该调用重置时间表的方法 */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(stringResource(R.string.reset_timetable))
            }
        }
    }
}

@Composable
fun QuickSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime, LocalTime, Int, Int, Int, Set<Int>) -> Unit
) {
    var morningStart by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var afternoonStart by remember { mutableStateOf(LocalTime.of(14, 0)) }
    var eveningStart by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var periodDuration by remember { mutableStateOf(45) }
    var breakDuration by remember { mutableStateOf(10) }
    var longBreakDuration by remember { mutableStateOf(20) }
    var breakPeriodsText by remember { mutableStateOf("2,4") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quick_setup)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.quick_setup_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 上午开始时间
                TimePickerField(
                    label = stringResource(R.string.morning_start_time),
                    time = morningStart,
                    onTimeChange = { newTime -> morningStart = newTime }
                )
                
                // 下午开始时间
                TimePickerField(
                    label = stringResource(R.string.afternoon_start_time),
                    time = afternoonStart,
                    onTimeChange = { newTime -> afternoonStart = newTime }
                )
                
                // 晚上开始时间
                TimePickerField(
                    label = stringResource(R.string.evening_start_time),
                    time = eveningStart,
                    onTimeChange = { newTime -> eveningStart = newTime }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 每节课时长
                NumberInputField(
                    label = stringResource(R.string.period_duration),
                    value = periodDuration,
                    onValueChange = { newValue -> periodDuration = newValue }
                )
                
                // 普通课间时长
                NumberInputField(
                    label = stringResource(R.string.break_duration),
                    value = breakDuration,
                    onValueChange = { newValue -> breakDuration = newValue }
                )
                
                // 大课间时长
                NumberInputField(
                    label = stringResource(R.string.long_break_duration),
                    value = longBreakDuration,
                    onValueChange = { newValue -> longBreakDuration = newValue }
                )
                
                // 大课间位置
                BreakPeriodsField(
                    label = stringResource(R.string.break_periods),
                    value = breakPeriodsText,
                    onValueChange = { newValue -> breakPeriodsText = newValue }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 解析大课间位置
                    val breakPeriods = breakPeriodsText
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                        .toSet()
                    
                    onConfirm(
                        morningStart,
                        afternoonStart,
                        eveningStart,
                        periodDuration,
                        breakDuration,
                        longBreakDuration,
                        breakPeriods
                    )
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

@Composable
fun TimePickerField(
    label: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")))
        }
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { selectedTime ->
                onTimeChange(selectedTime)
                showTimePicker = false
            },
            initialTime = time
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    initialTime: LocalTime
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
                
                // 更新 selectedTime 以反映 TimePicker 的当前值
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

@Composable
fun NumberInputField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
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
            Text(stringResource(R.string.minutes))
        }
    }
}

@Composable
fun BreakPeriodsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
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