package top.nefeli.schedule.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import top.nefeli.schedule.view.dialogs.PeriodInputDialog
import top.nefeli.schedule.viewmodel.ScheduleViewModel
import top.nefeli.schedule.viewmodel.ScheduleViewModelFactory
import top.nefeli.schedule.viewmodel.SettingsViewModel
import top.nefeli.schedule.viewmodel.SettingsViewModelFactory
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodManagementScreen(
    scheduleViewModelFactory: ScheduleViewModelFactory,
    settingsViewModelFactory: SettingsViewModelFactory,
    onBack: () -> Unit,
    navigateToBatchPeriodSetup: () -> Unit = {},
) {
    val scheduleViewModel: ScheduleViewModel = viewModel(factory = scheduleViewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)
    val periods by scheduleViewModel.period.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var periodToEdit by remember { mutableStateOf<Period?>(null) }
    var periodToDelete by remember { mutableStateOf<Period?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.period_schedule, "作息时间")) },
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
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (periods.isEmpty()) {
                EmptyPeriodsView(
                    onAddPeriod = { showAddDialog = true },
                    onBatchSetup = navigateToBatchPeriodSetup
                )
            } else {
                PeriodsListView(
                    periods = periods,
                    onEditPeriod = { periodToEdit = it },
                    onDeletePeriod = { periodToDelete = it },
                    onBatchSetup = navigateToBatchPeriodSetup
                )
            }
        }
    }

    // 添加或编辑对话框
    periodToEdit?.let { period ->
        PeriodInputDialog(
            period = period,
            numberOfPeriods = settings.numberOfPeriods,
            onConfirm = { updatedPeriod ->
                android.util.Log.d(
                    "PeriodManagementScreen",
                    "确认更新时段: id=${updatedPeriod.id}, name=${updatedPeriod.name}, startTime=${updatedPeriod.startTime}, endTime=${updatedPeriod.endTime}"
                )
                scheduleViewModel.updatePeriod(updatedPeriod)
                periodToEdit = null
            },
            onDismiss = { periodToEdit = null }
        )
    }

    if (showAddDialog) {
        PeriodInputDialog(
            period = Period(
                name = "",
                startTime = LocalTime.of(8, 0),
                endTime = LocalTime.of(8, 45),
                periodType = "",
                sortOrder = if (periods.isNotEmpty()) periods.size + 1 else 1
            ),
            numberOfPeriods = settings.numberOfPeriods,
            onConfirm = { newPeriod ->
                android.util.Log.d(
                    "PeriodManagementScreen",
                    "确认添加时段: id=${newPeriod.id}, name=${newPeriod.name}, startTime=${newPeriod.startTime}, endTime=${newPeriod.endTime}"
                )
                scheduleViewModel.addPeriod(newPeriod)
                // 更新设置中的课程节数
                settingsViewModel.updateSettings(settings.copy(numberOfPeriods = periods.size + 1))
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // 删除确认对话框
    periodToDelete?.let { period ->
        AlertDialog(
            onDismissRequest = { periodToDelete = null },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.confirm_delete_period, period.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        android.util.Log.d(
                            "PeriodManagementScreen",
                            "确认删除时段: id=${period.id}, name=${period.name}, startTime=${period.startTime}, endTime=${period.endTime}"
                        )
                        scheduleViewModel.deletePeriod(period)
                        // 更新设置中的课程节数
                        settingsViewModel.updateSettings(settings.copy(numberOfPeriods = periods.size - 1))
                        periodToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { periodToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun EmptyPeriodsView(
    onAddPeriod: () -> Unit,
    onBatchSetup: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_periods_yet),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddPeriod) {
            Text(stringResource(R.string.add_period))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBatchSetup) {
            Text(stringResource(R.string.batch_period_setup))
        }
    }
}

@Composable
fun PeriodsListView(
    periods: List<Period>,
    onEditPeriod: (Period) -> Unit,
    onDeletePeriod: (Period) -> Unit,
    onBatchSetup: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(periods.sortedBy { it.sortOrder }) { period ->
            PeriodItem(
                period = period,
                onEdit = { onEditPeriod(period) },
                onDelete = { onDeletePeriod(period) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBatchSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.batch_period_setup))
            }
        }
    }
}

@Composable
fun PeriodItem(
    period: Period,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = period.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${period.startTime} - ${period.endTime}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (period.periodType.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = period.periodType,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}