package com.example.apiarymanager.presentation.aianalysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.apiarymanager.domain.model.AnalysisType
import com.example.apiarymanager.domain.model.HivePhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: AiAnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AiAnalysisEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (uiState.step) {
                        AiAnalysisStep.SELECT_PHOTO -> "Wybierz zdjęcie"
                        AiAnalysisStep.FORM        -> "Parametry analizy"
                        AiAnalysisStep.ANALYZING   -> "Analiza AI"
                        AiAnalysisStep.RESULT      -> "Wynik analizy"
                    })
                },
                navigationIcon = {
                    if (uiState.step != AiAnalysisStep.ANALYZING) {
                        IconButton(onClick = {
                            if (uiState.step == AiAnalysisStep.FORM) viewModel.onBackToPhotoSelect()
                            else viewModel.onBackClick()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isUploading) {
            Box(
                modifier        = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text      = "Przesyłanie zdjęć na serwer…",
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (uiState.step) {
                AiAnalysisStep.SELECT_PHOTO -> SelectPhotoStep(
                    photos   = uiState.photos,
                    onSelect = viewModel::onPhotoSelected,
                    modifier = Modifier.padding(innerPadding)
                )
                AiAnalysisStep.FORM -> FormStep(
                    uiState              = uiState,
                    onNoteChange         = viewModel::onNoteChange,
                    onAnalysisTypeChange = viewModel::onAnalysisTypeChange,
                    onRunAnalysis        = viewModel::onRunAnalysis,
                    modifier             = Modifier.padding(innerPadding)
                )
                AiAnalysisStep.ANALYZING -> AnalyzingStep(modifier = Modifier.padding(innerPadding))
                AiAnalysisStep.RESULT    -> ResultStep(
                    uiState  = uiState,
                    onFinish = viewModel::onFinish,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// ─── Step 1: Select photo ─────────────────────────────────────────────────────

@Composable
private fun SelectPhotoStep(
    photos: List<HivePhoto>,
    onSelect: (HivePhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Outlined.Psychology, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text(
                    text      = "Brak zdjęć dla tego ula.\nDodaj zdjęcia podczas tworzenia przeglądu.",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    Column(modifier = modifier) {
        Text(
            text     = "Wybierz zdjęcie do analizy",
            style    = MaterialTheme.typography.bodyMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyVerticalGrid(
            columns               = GridCells.Fixed(3),
            contentPadding        = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement   = Arrangement.spacedBy(2.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                AsyncImage(
                    model              = photo.localPath,
                    contentDescription = "Zdjęcie",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onSelect(photo) }
                )
            }
        }
    }
}

// ─── Step 2: Form ─────────────────────────────────────────────────────────────

@Composable
private fun FormStep(
    uiState: AiAnalysisUiState,
    onNoteChange: (String) -> Unit,
    onAnalysisTypeChange: (AnalysisType) -> Unit,
    onRunAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Selected photo preview
        uiState.selectedPhoto?.let { photo ->
            AsyncImage(
                model              = photo.localPath,
                contentDescription = "Wybrane zdjęcie",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            )
        }

        Spacer(Modifier.height(16.dp))

        Text("Typ analizy", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Column(modifier = Modifier.selectableGroup()) {
            AnalysisType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.analysisType == type,
                            onClick  = { onAnalysisTypeChange(type) },
                            role     = Role.RadioButton
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = uiState.analysisType == type, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(type.displayName(), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Notatka (opcjonalna)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value         = uiState.note,
            onValueChange = onNoteChange,
            placeholder   = { Text("Dodatkowy kontekst dla modelu AI…") },
            minLines      = 3,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onRunAnalysis,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Outlined.Psychology, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Uruchom analizę AI", style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ─── Step 3: Analyzing ────────────────────────────────────────────────────────

@Composable
private fun AnalyzingStep(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(24.dp))
            Text(
                text       = "Trwa analiza wspierana przez AI…",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Oczekuję na odpowiedź z serwera",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Step 4: Result ───────────────────────────────────────────────────────────

@Composable
private fun ResultStep(
    uiState: AiAnalysisUiState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text       = "Wynik analizy",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text  = uiState.resultMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Zakończ", style = MaterialTheme.typography.titleMedium)
        }
    }
}
