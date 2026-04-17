package com.example.meetingmemo.ui.importaudio

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meetingmemo.ui.common.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioImportScreen(
    onBack: () -> Unit,
    onImportCompleted: () -> Unit,
    viewModel: AudioImportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.onAudioFileSelected(uri)
        }
    }

    LaunchedEffect(uiState.importState) {
        if (uiState.importState is UiState.Error) {
            snackbarHostState.showSnackbar((uiState.importState as UiState.Error).message)
            viewModel.clearImportState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("음성 파일 가져오기") },
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
            Text(
                text = "휴대폰에 저장된 음성 파일을 선택하면 서버에서 전사와 요약을 한 번에 처리합니다.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(
                onClick = { openDocumentLauncher.launch(arrayOf("audio/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("음성 파일 선택")
            }

            if (uiState.selectedFileName.isNotBlank()) {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = uiState.selectedFileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "크기: ${uiState.selectedFileSizeLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (uiState.selectedFileMimeType.isNotBlank()) {
                            Text(
                                text = "형식: ${uiState.selectedFileMimeType}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::processSelectedAudio,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isReadyToProcess && uiState.importState !is UiState.Loading,
            ) {
                if (uiState.importState is UiState.Loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("파일 업로드 후 정리하기")
                }
            }

            uiState.importedDraft?.let { draft ->
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = draft.title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(text = "전사 원문", style = MaterialTheme.typography.titleMedium)
                        Text(text = draft.rawText)
                        Text(text = "요약", style = MaterialTheme.typography.titleMedium)
                        Text(text = draft.summaryText)
                        if (draft.actionItems.isNotEmpty()) {
                            Text(
                                text = "액션 아이템: ${draft.actionItems.joinToString()}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (viewModel.cacheDraftForRecord()) {
                                        onImportCompleted()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("메모 화면으로 가져오기")
                        }
                    }
                }
            }
        }
    }
}
