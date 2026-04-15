package com.example.apiarymanager.presentation.treatment

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentFormScreen(
    hiveId: Long,
    treatmentId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: TreatmentFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { when (it) { TreatmentFormEvent.NavigateBack -> onNavigateBack() } }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        viewModel.onDateChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") } }
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (treatmentId == null) "Nowe leczenie" else "Edytuj leczenie") },
                navigationIcon = { IconButton(onClick = viewModel::onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).imePadding().padding(16.dp)
        ) {
            // Date
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Data leczenia", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(uiState.date.format(fmt), style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(onClick = { showDatePicker = true }) { Text("Zmień") }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = uiState.medicineType, onValueChange = viewModel::onMedicineTypeChange, label = { Text("Rodzaj leku") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = uiState.dosage, onValueChange = viewModel::onDosageChange, label = { Text("Ilość / dawka") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = uiState.applicationMethod, onValueChange = viewModel::onApplicationMethodChange, label = { Text("Sposób podania") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = uiState.mortalityAfterTreatment, onValueChange = viewModel::onMortalityChange, label = { Text("Osyp po leczeniu") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::onSaveClick, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (uiState.isSaving) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                else Text("Zapisz", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
