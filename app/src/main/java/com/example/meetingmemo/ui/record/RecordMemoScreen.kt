package com.example.meetingmemo.ui.record

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meetingmemo.ui.common.UiState
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordMemoScreen(
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: RecordMemoViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()

            if (spokenText.isBlank()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("음성 입력 결과가 비어 있습니다.")
                }
            } else {
                viewModel.appendSpeechResult(spokenText)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchSpeechInput(
                onLaunch = { speechLauncher.launch(buildSpeechIntent()) },
                onError = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("음성 입력을 시작할 수 없습니다.")
                    }
                },
            )
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("마이크 권한이 거부되었습니다.")
            }
        }
    }

    LaunchedEffect(uiState.saveState) {
        when (val state = uiState.saveState) {
            is UiState.Success -> {
                onSaved(state.data)
                viewModel.clearSaveState()
            }

            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearSaveState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(uiState.summaryState) {
        if (uiState.summaryState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState.summaryState as UiState.Error).message)
            viewModel.clearSummaryState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 회의 메모") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchSpeechInput(
                            onLaunch = { speechLauncher.launch(buildSpeechIntent()) },
                            onError = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("음성 입력을 시작할 수 없습니다.")
                                }
                            },
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("음성 입력 시작")
            }

            Text(
                text = "STT 결과는 아래 원문 필드에 추가되며, 사용자가 자유롭게 수정할 수 있습니다.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("제목") },
                placeholder = { Text("요약 생성 시 자동 제안됩니다.") },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.rawText,
                onValueChange = viewModel::updateRawText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                label = { Text("원문") },
                placeholder = { Text("회의 내용을 입력하거나 음성으로 받아오세요.") },
            )

            OutlinedTextField(
                value = uiState.summaryText,
                onValueChange = viewModel::updateSummaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                label = { Text("요약") },
                placeholder = { Text("요약 생성 결과가 표시됩니다.") },
            )

            if (uiState.actionItems.isNotEmpty()) {
                Text(
                    text = "액션 아이템: ${uiState.actionItems.joinToString()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = viewModel::generateSummary,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.summaryState !is UiState.Loading,
            ) {
                if (uiState.summaryState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("요약 생성")
                }
            }

            Button(
                onClick = viewModel::saveMemo,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.rawText.isNotBlank() && uiState.saveState !is UiState.Loading,
            ) {
                if (uiState.saveState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("메모 저장")
                }
            }
        }
    }
}

private fun buildSpeechIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "회의 내용을 말씀해주세요.")
    }
}

private fun launchSpeechInput(
    onLaunch: () -> Unit,
    onError: () -> Unit,
) {
    runCatching { onLaunch() }.onFailure { onError() }
}
