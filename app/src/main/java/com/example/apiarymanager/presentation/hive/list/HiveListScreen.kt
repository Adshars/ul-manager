package com.example.apiarymanager.presentation.hive.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.Hive
import com.example.apiarymanager.domain.model.HiveStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveListScreen(
    apiaryId: Long,
    onNavigateToHiveDetail: (hiveId: Long) -> Unit,
    onNavigateToHiveForm: (apiaryId: Long, hiveId: Long?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: HiveListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HiveListEvent.NavigateToHiveDetail -> onNavigateToHiveDetail(event.hiveId)
                is HiveListEvent.NavigateToHiveForm   -> onNavigateToHiveForm(event.apiaryId, event.hiveId)
                HiveListEvent.NavigateBack            -> onNavigateBack()
                is HiveListEvent.ShowMessage          -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title       = { Text(uiState.apiary?.name ?: "Ule") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddHiveClick) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj ul")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.hives.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "Brak uli. Dodaj pierwszy ul.",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyColumn(
                modifier       = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.hives, key = { it.id }) { hive ->
                    HiveCard(
                        hive       = hive,
                        onClick    = { viewModel.onHiveClick(hive.id) },
                        onEdit     = { viewModel.onEditHiveClick(hive.id) },
                        onDelete   = { viewModel.onDeleteHive(hive.id) },
                        onMove     = { viewModel.onMoveHiveRequest(hive) }
                    )
                }
            }
        }
    }

    // Move hive dialog
    uiState.moveDialogHive?.let { hive ->
        MoveHiveDialog(
            hive         = hive,
            allApiaries  = uiState.allApiaries.filter { it.id != apiaryId },
            selectedId   = uiState.moveTargetApiaryId,
            onSelectApiary = viewModel::onMoveTargetSelected,
            onConfirm    = viewModel::onMoveConfirm,
            onDismiss    = viewModel::onMoveDismiss
        )
    }
}

@Composable
private fun HiveCard(
    hive: Hive,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector     = Icons.Filled.Hive,
                contentDescription = null,
                tint            = statusColor(hive.status),
                modifier        = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = "${hive.number}. ${hive.name}",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = hive.status.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor(hive.status)
                )
                if (hive.queenYear != null) {
                    Text(
                        text  = "Matka ${hive.queenYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onMove) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = "Przenieś ul", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edytuj", modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Usuń",
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun statusColor(status: HiveStatus) = when (status) {
    HiveStatus.ACTIVE -> MaterialTheme.colorScheme.primary
    HiveStatus.WEAK   -> MaterialTheme.colorScheme.secondary
    HiveStatus.DEAD   -> MaterialTheme.colorScheme.error
    HiveStatus.SOLD   -> MaterialTheme.colorScheme.onSurfaceVariant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveHiveDialog(
    hive: Hive,
    allApiaries: List<com.example.apiarymanager.domain.model.Apiary>,
    selectedId: Long?,
    onSelectApiary: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedApiary = allApiaries.firstOrNull { it.id == selectedId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Przenieś ul ${hive.name}") },
        text    = {
            Column {
                Text("Wybierz docelową pasiekę:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded        = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value             = selectedApiary?.name ?: "",
                        onValueChange     = {},
                        readOnly          = true,
                        label             = { Text("Pasieka") },
                        trailingIcon      = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier          = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded        = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allApiaries.forEach { apiary ->
                            DropdownMenuItem(
                                text    = { Text(apiary.name) },
                                onClick = {
                                    onSelectApiary(apiary.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = selectedId != null) { Text("Przenieś") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}
