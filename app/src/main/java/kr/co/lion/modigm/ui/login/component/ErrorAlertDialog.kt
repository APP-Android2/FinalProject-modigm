package kr.co.lion.modigm.ui.login.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorAlertDialog(
    title: String,
    message: String,
    confirmButtonText: String = "확인",
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit = {},
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = {
            if (dismissOnBackPress || dismissOnClickOutside) {
                onDismissRequest()
            }
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = {
                onConfirmClick()
                onDismissRequest()
            }) {
                Text(text = confirmButtonText)
            }
        }
    )
}