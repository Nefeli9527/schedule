package top.nefeli.schedule.view.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import top.nefeli.schedule.model.Period
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodInputDialog(
    period: Period,
    numberOfPeriods: Int,
    onConfirm: (Period) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(period.name) }
    var startTime by remember { mutableStateOf(period.startTime) }
    var endTime by remember { mutableStateOf(period.endTime) }
    var periodType by remember { mutableStateOf(period.periodType) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { selectedTime ->
                startTime = selectedTime
                showStartTimePicker = false
            },
            initialTime = startTime
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { selectedTime ->
                endTime = selectedTime
                showEndTimePicker = false
            },
            initialTime = endTime
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (period.id == 0L)
                    stringResource(R.string.add_period)
                else
                    stringResource(R.string.edit_period)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 节次名称（编辑现有时段时显示为不可编辑文本）
                if (period.id != 0L) {
                    Text(
                        text = stringResource(R.string.period_name),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = name,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 时间段选择 - 在同一行显示开始和结束时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.start_time),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { showStartTimePicker = true }) {
                            Text(startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.end_time),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = { showEndTimePicker = true }) {
                            Text(endTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 时段类型 - 使用选择框
                Text(
                    text = stringResource(R.string.period_type),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                PeriodTypeSelector(
                    selectedType = periodType,
                    onTypeSelected = { periodType = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 自动生成节次名称
                    val autoName = when {
                        period.id != 0L -> name // 编辑现有时段时使用原有名称
                        else -> {
                            // 添加新时段时根据时段类型自动生成名称
                            val typeLabel = when (periodType) {
                                "上午" -> "上午"
                                "中午" -> "中午"
                                "下午" -> "下午"
                                else -> "时段"
                            }
                            "$typeLabel${"节次"}"
                        }
                    }

                    val updatedPeriod = period.copy(
                        name = autoName,
                        startTime = startTime,
                        endTime = endTime,
                        periodType = periodType
                    )
                    onConfirm(updatedPeriod)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
) {
    val periodTypes = listOf(
        "上午" to "上午",
        "中午" to "中午",
        "下午" to "下午"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        periodTypes.forEach { (label, value) ->
            FilterChip(
                selected = selectedType == value,
                onClick = { onTypeSelected(value) },
                label = { Text(label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
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
                val state = androidx.compose.material3.rememberTimePickerState(
                    initialHour = selectedTime.hour,
                    initialMinute = selectedTime.minute,
                    is24Hour = true
                )
                androidx.compose.material3.TimePicker(
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