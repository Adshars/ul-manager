package com.example.apiarymanager.presentation.inspection

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.apiarymanager.domain.model.ColonyStrength
import com.example.apiarymanager.presentation.theme.ApiaryManagerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// ─── Screen entry point ───────────────────────────────────────────────────────

@Composable
fun InspectionFormScreen(
    hiveId: Long,
    inspectionId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: (outputDir: String) -> Unit = {},
    viewModel: InspectionFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                InspectionFormEvent.NavigateBack          -> onNavigateBack()
                is InspectionFormEvent.ShowMessage        -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            InspectionFormTopBar(
                isEditMode = inspectionId != null,
                onBackClick = viewModel::onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            InspectionFormContent(
                uiState           = uiState,
                viewModel         = viewModel,
                onNavigateToCamera = onNavigateToCamera,
                modifier          = Modifier.padding(innerPadding)
            )
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectionFormTopBar(
    isEditMode: Boolean,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(if (isEditMode) "Edytuj przegląd" else "Nowy przegląd") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
            }
        }
    )
}

// ─── Scrollable form ──────────────────────────────────────────────────────────

@Composable
private fun InspectionFormContent(
    uiState: InspectionFormUiState,
    viewModel: InspectionFormViewModel,
    onNavigateToCamera: (outputDir: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> -> viewModel.onPhotosFromGallery(uris) }

    // Camera permission launcher — navigates to CameraScreen once granted
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
                ?: context.cacheDir.absolutePath
            onNavigateToCamera(dir)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(bottom = 32.dp)
    ) {
        // ── 1. Date ───────────────────────────────────────────────────────────
        DateSection(
            date     = uiState.date,
            onDateChange = viewModel::onDateChange
        )

        // ── 2. Colony status checkboxes ───────────────────────────────────────
        FormSection(title = "Stan rodziny") {
            LabeledCheckbox(
                checked         = uiState.queenSeen,
                onCheckedChange = viewModel::onQueenSeenChange,
                label           = "Matka widoczna"
            )
            LabeledCheckbox(
                checked         = uiState.broodSeen,
                onCheckedChange = viewModel::onBroodSeenChange,
                label           = "Czerw widoczny"
            )
            LabeledCheckbox(
                checked         = uiState.queenCellsSeen,
                onCheckedChange = viewModel::onQueenCellsSeenChange,
                label           = "Mateczniki widoczne"
            )
        }

        // ── 3. Colony strength slider ─────────────────────────────────────────
        FormSection(title = "Siła rodziny") {
            ColonyStrengthSlider(
                strength  = uiState.colonyStrength,
                onChange  = viewModel::onColonyStrengthChange
            )
        }

        // ── 4. Frame management ───────────────────────────────────────────────
        FormSection(title = "Zmiany w ulu") {
            FrameManagementSection(
                uiState  = uiState,
                viewModel = viewModel
            )
        }

        // ── 5. Problems ───────────────────────────────────────────────────────
        FormSection(title = "Zaobserwowane problemy") {
            OutlinedTextField(
                value       = uiState.problems,
                onValueChange = viewModel::onProblemsChange,
                placeholder = { Text("Opisz zaobserwowane problemy (waroza, choroby, słaba rodzina...)") },
                minLines    = 3,
                modifier    = Modifier.fillMaxWidth()
            )
        }

        // ── 6. Notes ──────────────────────────────────────────────────────────
        FormSection(title = "Notatki") {
            OutlinedTextField(
                value       = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                placeholder = { Text("Dodatkowe uwagi i obserwacje...") },
                minLines    = 3,
                modifier    = Modifier.fillMaxWidth()
            )
        }

        // ── 7. Photos ─────────────────────────────────────────────────────────
        FormSection(title = "Zdjęcia") {
            // Action buttons row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick  = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
                                ?: context.cacheDir.absolutePath
                            onNavigateToCamera(dir)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aparat")
                }
                FilledTonalButton(
                    onClick  = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Galeria")
                }
            }

            // Thumbnails
            if (uiState.photoPaths.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.photoPaths.forEach { path ->
                        Box(modifier = Modifier.size(90.dp)) {
                            AsyncImage(
                                model             = path,
                                contentDescription = "Zdjęcie",
                                contentScale      = ContentScale.Crop,
                                modifier          = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                            )
                            // Delete button overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.55f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
                                    )
                                    .clickable { viewModel.onPhotoRemoved(path) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Filled.Close,
                                    contentDescription = "Usuń zdjęcie",
                                    tint               = Color.White,
                                    modifier           = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Save button ───────────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))
        Button(
            onClick  = viewModel::onSaveClick,
            enabled  = !uiState.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color        = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth  = 2.dp,
                    modifier     = Modifier.size(24.dp)
                )
            } else {
                Text("Zapisz przegląd", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// ─── Form section wrapper ─────────────────────────────────────────────────────

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text     = title,
            style    = MaterialTheme.typography.labelLarge,
            color    = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                content  = content
            )
        }
    }
}

// ─── Date section ─────────────────────────────────────────────────────────────

private val displayDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSection(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDateChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text     = "Data przeglądu",
            style    = MaterialTheme.typography.labelLarge,
            color    = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector     = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint            = MaterialTheme.colorScheme.primary,
                    modifier        = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text  = date.format(displayDateFormatter),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { showPicker = true }) { Text("Zmień") }
            }
        }
    }
}

// ─── Checkbox helper ──────────────────────────────────────────────────────────

@Composable
private fun LabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

// ─── Colony strength slider ───────────────────────────────────────────────────

@Composable
private fun ColonyStrengthSlider(
    strength: ColonyStrength,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Aktualna siła:", style = MaterialTheme.typography.bodyMedium)
            Text(
                text       = strength.displayName(),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = strengthColor(strength)
            )
        }

        Slider(
            value          = strength.ordinal.toFloat(),
            onValueChange  = { onChange(it.roundToInt()) },
            valueRange     = 0f..4f,
            steps          = 3,   // 5 positions: 0,1,2,3,4
            modifier       = Modifier.fillMaxWidth()
        )

        // Labels under the slider — one per strength value
        Row(modifier = Modifier.fillMaxWidth()) {
            ColonyStrength.entries.forEach { s ->
                Text(
                    text      = s.shortLabel(),
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style     = MaterialTheme.typography.labelSmall,
                    fontWeight = if (s == strength) FontWeight.Bold else FontWeight.Normal,
                    color     = if (s == strength)
                        strengthColor(strength)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun strengthColor(strength: ColonyStrength): Color = when (strength) {
    ColonyStrength.CRITICAL   -> MaterialTheme.colorScheme.error
    ColonyStrength.WEAK       -> MaterialTheme.colorScheme.secondary
    ColonyStrength.NORMAL     -> MaterialTheme.colorScheme.onSurface
    ColonyStrength.STRONG     -> MaterialTheme.colorScheme.primary
    ColonyStrength.VERY_STRONG -> MaterialTheme.colorScheme.tertiary
}

// ─── Frame management ─────────────────────────────────────────────────────────

@Composable
private fun FrameManagementSection(
    uiState: InspectionFormUiState,
    viewModel: InspectionFormViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Toggle row
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "Rejestruj zmiany ramek",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text  = "Nadstawki, susz, węzy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked         = uiState.framesManagementEnabled,
                onCheckedChange = viewModel::onFramesManagementToggle
            )
        }

        // Collapsible counters
        AnimatedVisibility(
            visible = uiState.framesManagementEnabled,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                CounterStepper(
                    label       = "Nadstawki dodane",
                    value       = uiState.superboxesAdded,
                    onDecrement = { viewModel.onSuperboxesAddedChange(uiState.superboxesAdded - 1) },
                    onIncrement = { viewModel.onSuperboxesAddedChange(uiState.superboxesAdded + 1) }
                )
                Spacer(Modifier.height(8.dp))
                CounterStepper(
                    label       = "Nadstawki usunięte",
                    value       = uiState.superboxesRemoved,
                    onDecrement = { viewModel.onSuperboxesRemovedChange(uiState.superboxesRemoved - 1) },
                    onIncrement = { viewModel.onSuperboxesRemovedChange(uiState.superboxesRemoved + 1) }
                )
                Spacer(Modifier.height(8.dp))
                CounterStepper(
                    label       = "Ramki z suszem",
                    value       = uiState.dryCombFrames,
                    onDecrement = { viewModel.onDryCombFramesChange(uiState.dryCombFrames - 1) },
                    onIncrement = { viewModel.onDryCombFramesChange(uiState.dryCombFrames + 1) }
                )
                Spacer(Modifier.height(8.dp))
                CounterStepper(
                    label       = "Węzy",
                    value       = uiState.foundationFrames,
                    onDecrement = { viewModel.onFoundationFramesChange(uiState.foundationFrames - 1) },
                    onIncrement = { viewModel.onFoundationFramesChange(uiState.foundationFrames + 1) }
                )
            }
        }
    }
}

// ─── Counter stepper component ────────────────────────────────────────────────

@Composable
private fun CounterStepper(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    minValue: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        FilledTonalIconButton(
            onClick  = onDecrement,
            enabled  = value > minValue
        ) {
            Icon(
                imageVector     = Icons.Filled.Remove,
                contentDescription = "Zmniejsz $label",
                modifier        = Modifier.size(18.dp)
            )
        }
        Text(
            text      = value.toString(),
            style     = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier  = Modifier.width(44.dp)
        )
        FilledTonalIconButton(onClick = onIncrement) {
            Icon(
                imageVector     = Icons.Filled.Add,
                contentDescription = "Zwiększ $label",
                modifier        = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ColonyStrengthSliderPreview() {
    ApiaryManagerTheme {
        ElevatedCard(modifier = Modifier.padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                ColonyStrengthSlider(strength = ColonyStrength.STRONG, onChange = {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterStepperPreview() {
    ApiaryManagerTheme {
        Column(Modifier.padding(16.dp)) {
            CounterStepper(label = "Nadstawki dodane", value = 2, onDecrement = {}, onIncrement = {})
        }
    }
}
