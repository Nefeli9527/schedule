package top.nefeli.schedule.view.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import top.nefeli.schedule.R

/**
 * 重置时间表确认对话框
 *
 * @param onConfirm 确认重置的回调
 * @param onDismiss 取消重置的回调
 */
@Composable
fun ResetTimetableDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_timetable)) },
        text = { Text(stringResource(R.string.reset_timetable_confirm_message)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm()
                onDismiss()
            }) {
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