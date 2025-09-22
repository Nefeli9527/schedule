package top.nefeli.schedule.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.nefeli.schedule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelector(
    totalWeeks: Int,
    selectedWeeks: Set<Int>,
    onWeeksChange: (Set<Int>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 周次选择控制部分
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.selected_weeks_count, selectedWeeks.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { onWeeksChange((1..totalWeeks).toSet()) },
                        enabled = selectedWeeks.size < totalWeeks
                    ) {
                        Text(stringResource(R.string.select_all))
                    }

                    TextButton(
                        onClick = { onWeeksChange(emptySet()) },
                        enabled = selectedWeeks.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.clear_all))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 显示周次选择器，每行仅展示5格
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 计算需要显示的行数
            val rows = (totalWeeks + 4) / 5

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (col in 0 until 5) {
                        val weekNumber = row * 5 + col + 1
                        if (weekNumber <= totalWeeks) {
                            FilterChip(
                                selected = weekNumber in selectedWeeks,
                                onClick = {
                                    onWeeksChange(
                                        if (weekNumber in selectedWeeks) {
                                            selectedWeeks - weekNumber
                                        } else {
                                            selectedWeeks + weekNumber
                                        }
                                    )
                                },
                                label = { Text(stringResource(R.string.week_number, weekNumber)) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeekSelectorPreview() {
    var selectedWeeks by remember { mutableStateOf(setOf(1, 2, 3, 5, 7, 8, 10, 12, 15, 18, 20)) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.padding(16.dp)
        ) {
            WeekSelector(
                totalWeeks = 22,
                selectedWeeks = selectedWeeks,
                onWeeksChange = { selectedWeeks = it }
            )
        }
    }
}