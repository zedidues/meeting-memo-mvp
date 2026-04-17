package com.example.meetingmemo.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meetingmemo.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    onBack: () -> Unit,
    viewModel: MemoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val emailSendState by viewModel.emailSendState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(emailSendState) {
        when (val state = emailSendState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.data)
                viewModel.clearEmailState()
                showDialog = false
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearEmailState()
            }

            else -> Unit
        }
    }

    if (showDialog) {
        SendEmailDialog(
            email = uiState.emailInput,
            onEmailChange = viewModel::updateEmailInput,
            onDismiss = { showDialog = false },
            onSend = viewModel::sendEmail,
            onSaveDefault = viewModel::saveDefaultEmail,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메모 상세") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val memo = uiState.memo
        if (memo == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("메모를 불러오는 중입니다.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = memo.title, style = MaterialTheme.typography.headlineSmall)

            Text(text = "원문", style = MaterialTheme.typography.titleMedium)
            Text(text = memo.rawText.ifBlank { "원문이 없습니다." })

            Text(text = "요약", style = MaterialTheme.typography.titleMedium)
            Text(text = memo.summaryText.ifBlank { "요약이 없습니다." })

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (emailSendState is UiState.Loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("이메일 발송")
                }
            }

            if (uiState.defaultEmail.isNotBlank()) {
                Text(
                    text = "기본 이메일: ${uiState.defaultEmail}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
