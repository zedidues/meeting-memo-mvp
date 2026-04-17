package com.example.meetingmemo.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SendEmailDialog(
    email: String,
    isSending: Boolean,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    onSaveDefault: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        title = { Text("이메일 발송") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("수신 이메일") },
                    singleLine = true,
                    enabled = !isSending,
                )
                Button(
                    onClick = onSaveDefault,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                ) {
                    Text("기본 이메일로 저장")
                }
                if (isSending) {
                    Text(
                        text = "발송 중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSend, enabled = !isSending) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("발송")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text("취소")
            }
        },
    )
}
