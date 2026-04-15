package com.example.apiarymanager.presentation.task

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.TaskPriority
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: TaskFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { TaskFormEvent.NavigateBack -> onNavigateBack() } }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDueDateChange(
                        pickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    )
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") } }
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.uiState.value.isSaving) "Zapisywanie…" else "Zadanie") },
                navigationIcon = { IconButton(onClick = viewModel::onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).imePadding().padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.title, onValueChange = viewModel::onTitleChange,
                label = { Text("Nazwa zadania") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.description, onValueChange = viewModel::onDescriptionChange,
                label = { Text("Notatka") }, minLines = 3, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Zakres zadania")
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = uiState.scope == TaskScope.GENERAL, onClick = { viewModel.onScopeChange(TaskScope.GENERAL) }, label = { Text("Ogólne") })
                FilterChip(selected = uiState.scope == TaskScope.APIARY,  onClick = { viewModel.onScopeChange(TaskScope.APIARY) },  label = { Text("Pasieka") })
                FilterChip(selected = uiState.scope == TaskScope.HIVE,    onClick = { viewModel.onScopeChange(TaskScope.HIVE) },    label = { Text("Ul") })
            }

            // Apiary picker (for APIARY and HIVE scope)
            if (uiState.scope != TaskScope.GENERAL) {
                Spacer(Modifier.height(8.dp))
                ApiaryDropdown(
                    label      = "Wybierz pasiekę",
                    apiaries   = uiState.allApiaries,
                    selectedId = uiState.selectedApiaryId,
                    onSelect   = viewModel::onApiarySelected
                )
            }
            // Hive picker (for HIVE scope)
            if (uiState.scope == TaskScope.HIVE && uiState.selectedApiaryId != null) {
                Spacer(Modifier.height(8.dp))
                GenericDropdown(
                    label      = "Wybierz ul",
                    items      = uiState.hivesForApiary.map { it.id to "${it.number}. ${it.name}" },
                    selectedId = uiState.selectedHiveId,
                    onSelect   = viewModel::onHiveSelected
                )
            }

            Spacer(Modifier.height(12.dp))
            SectionLabel("Data wykonania")
            OutlinedTextField(
                value         = uiState.dueDate?.format(fmt) ?: "Brak terminu",
                onValueChange = {},
                readOnly      = true,
                modifier      = Modifier.fillMaxWidth().clickable { showDatePicker = true }
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Priorytet")
            PriorityDropdown(selected = uiState.priority, onSelect = viewModel::onPriorityChange)

            Spacer(Modifier.height(20.dp))
            Button(onClick = viewModel::onSaveClick, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (uiState.isSaving) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                else Text("Zapisz zadanie", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiaryDropdown(label: String, apiaries: List<com.example.apiarymanager.domain.model.Apiary>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = apiaries.firstOrNull { it.id == selectedId }?.name ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selectedName, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            apiaries.forEach { a -> DropdownMenuItem(text = { Text(a.name) }, onClick = { onSelect(a.id); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenericDropdown(label: String, items: List<Pair<Long, String>>, selectedId: Long?, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = items.firstOrNull { it.first == selectedId }?.second ?: ""
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selectedName, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { (id, name) -> DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityDropdown(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = runCatching { TaskPriority.valueOf(selected) }.getOrDefault(TaskPriority.MEDIUM)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = current.displayName(), onValueChange = {}, readOnly = true, label = { Text("Priorytet") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskPriority.entries.forEach { p -> DropdownMenuItem(text = { Text(p.displayName()) }, onClick = { onSelect(p.name); expanded = false }) }
        }
    }
}
