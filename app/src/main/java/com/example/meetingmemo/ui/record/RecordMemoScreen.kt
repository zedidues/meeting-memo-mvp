package com.example.meetingmemo.ui.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meetingmemo.ui.common.UiState
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
            viewModel.stopRecording()
        }
    }

    // 연속 음성 인식 루프 - isRecording이 바뀔 때마다 재시작
    LaunchedEffect(uiState.isRecording) {
        if (!uiState.isRecording) {
            speechRecognizer.stopListening()
            return@LaunchedEffect
        }

        while (true) {
            suspendCancellableCoroutine { cont ->
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onResults(bundle: Bundle) {
                        val text = bundle
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) viewModel.appendSpeechResult(text)
                        else viewModel.updateInterimText("")
                        if (!cont.isCompleted) cont.resume(Unit)
                    }

                    override fun onPartialResults(bundle: Bundle) {
                        val partial = bundle
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull().orEmpty()
                        viewModel.updateInterimText(partial)
                    }

                    override fun onError(error: Int) {
                        viewModel.updateInterimText("")
                        if (!cont.isCompleted) cont.resume(Unit)
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                }
                speechRecognizer.startListening(intent)

                cont.invokeOnCancellation { speechRecognizer.stopListening() }
            }
            // 세그먼트 간 짧은 대기 후 자동 재시작
            kotlinx.coroutines.delay(200L)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
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
                    TextButton(onClick = {
                        viewModel.stopRecording()
                        onBack()
                    }) {
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
            // 녹음 컨트롤
            RecordingControl(
                isRecording = uiState.isRecording,
                onStart = {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.startRecording()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStop = viewModel::stopRecording,
            )

            // 실시간 인식 중인 텍스트
            if (uiState.interimText.isNotBlank()) {
                Text(
                    text = "인식 중: ${uiState.interimText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }

            // 자동 요약 안내
            if (uiState.isRecording) {
                Text(
                    text = "1분마다 자동으로 요약됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

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
                placeholder = { Text("녹음 시작 후 음성이 여기에 쌓입니다.") },
            )

            // 수동 요약 버튼
            Button(
                onClick = viewModel::generateSummary,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.summaryState !is UiState.Loading,
            ) {
                if (uiState.summaryState is UiState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("요약 생성 중...")
                    }
                } else {
                    Text("지금 요약 생성")
                }
            }

            if (uiState.summaryText.isNotBlank()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("요약", style = MaterialTheme.typography.titleMedium)
                        Text(uiState.summaryText)
                        if (uiState.actionItems.isNotEmpty()) {
                            Text("내가 해야할 것", style = MaterialTheme.typography.titleMedium)
                            uiState.actionItems.forEachIndexed { index, item ->
                                Text("${index + 1}. $item", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.stopRecording()
                    viewModel.saveMemo()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.rawText.isNotBlank() && uiState.saveState !is UiState.Loading,
            ) {
                if (uiState.saveState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("메모 저장")
                }
            }
        }
    }
}

@Composable
private fun RecordingControl(
    isRecording: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    if (isRecording) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = "녹음 중...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("녹음 중지")
            }
        }
    } else {
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("녹음 시작")
        }
    }
}
