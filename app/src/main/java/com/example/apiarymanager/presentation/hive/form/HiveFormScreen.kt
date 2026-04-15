package com.example.apiarymanager.presentation.hive.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.HiveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveFormScreen(
    apiaryId: Long,
    hiveId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: HiveFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HiveFormEvent.NavigateBack         -> onNavigateBack()
                is HiveFormEvent.ShowMessage       -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (hiveId == null) "Nowy ul" else "Edytuj ul") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FormSection("Podstawowe informacje") {
                OutlinedTextField(
                    value         = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label         = { Text("Nazwa ula") },
                    isError       = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = uiState.number,
                    onValueChange = viewModel::onNumberChange,
                    label         = { Text("Numer ula") },
                    isError       = uiState.numberError != null,
                    supportingText = uiState.numberError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Status dropdown
                StatusDropdown(
                    selected  = uiState.status,
                    onSelect  = viewModel::onStatusChange,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            FormSection("Szczegóły") {
                OutlinedTextField(
                    value         = uiState.frameType,
                    onValueChange = viewModel::onFrameTypeChange,
                    label         = { Text("Typ ramek") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = uiState.superboxCount,
                    onValueChange = viewModel::onSuperboxCountChange,
                    label         = { Text("Liczba nadstawek") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = uiState.queenYear,
                    onValueChange = viewModel::onQueenYearChange,
                    label         = { Text("Rok matki") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = uiState.queenOrigin,
                    onValueChange = viewModel::onQueenOriginChange,
                    label         = { Text("Pochodzenie rodziny") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            FormSection("Notatki") {
                OutlinedTextField(
                    value         = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label         = { Text("Notatki") },
                    minLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = viewModel::onSaveClick,
                enabled  = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(24.dp)
                    )
                } else {
                    Text("Zapisz", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Text(
        text       = title,
        style      = MaterialTheme.typography.labelLarge,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val selectedStatus = runCatching { HiveStatus.valueOf(selected) }.getOrDefault(HiveStatus.ACTIVE)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value           = selectedStatus.displayName(),
            onValueChange   = {},
            readOnly        = true,
            label           = { Text("Status") },
            trailingIcon    = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier        = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            HiveStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text    = { Text(status.displayName()) },
                    onClick = { onSelect(status.name); expanded = false }
                )
            }
        }
    }
}
