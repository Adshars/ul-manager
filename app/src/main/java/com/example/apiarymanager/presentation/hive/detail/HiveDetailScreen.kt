package com.example.apiarymanager.presentation.hive.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.Feeding
import com.example.apiarymanager.domain.model.HoneyHarvest
import com.example.apiarymanager.domain.model.Inspection
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.Treatment
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("pl"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailScreen(
    hiveId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToHiveForm: (apiaryId: Long, hiveId: Long) -> Unit,
    onNavigateToInspectionForm: (hiveId: Long, inspectionId: Long?) -> Unit,
    onNavigateToHarvestForm: (hiveId: Long, harvestId: Long?) -> Unit,
    onNavigateToTreatmentForm: (hiveId: Long, treatmentId: Long?) -> Unit,
    onNavigateToFeedingForm: (hiveId: Long, feedingId: Long?) -> Unit,
    onNavigateToTaskForm: (hiveId: Long, taskId: Long?) -> Unit,
    initialTab: Int = 0,
    viewModel: HiveDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HiveDetailEvent.NavigateBack ->
                    onNavigateBack()
                is HiveDetailEvent.NavigateToHiveForm ->
                    onNavigateToHiveForm(event.apiaryId, event.hiveId)
                is HiveDetailEvent.NavigateToInspectionForm ->
                    onNavigateToInspectionForm(event.hiveId, event.inspectionId)
                is HiveDetailEvent.NavigateToHarvestForm ->
                    onNavigateToHarvestForm(event.hiveId, event.harvestId)
                is HiveDetailEvent.NavigateToTreatmentForm ->
                    onNavigateToTreatmentForm(event.hiveId, event.treatmentId)
                is HiveDetailEvent.NavigateToFeedingForm ->
                    onNavigateToFeedingForm(event.hiveId, event.feedingId)
                is HiveDetailEvent.NavigateToTaskForm ->
                    onNavigateToTaskForm(event.hiveId, event.taskId)
                is HiveDetailEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(uiState.hive?.name ?: "Ul") },
                    navigationIcon = {
                        IconButton(onClick = viewModel::onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                        }
                    },
                    actions = {
                        IconButton(onClick = viewModel::onEditHiveClick) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edytuj ul")
                        }
                    }
                )
                val tabs = listOf("Szczegóły", "Przeglądy", "Miodobrania", "Leczenia", "Dokarmiania", "Zadania")
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                    tabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text     = { Text(label) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                1 -> FloatingActionButton(onClick = viewModel::onAddInspection)  { Icon(Icons.Filled.Add, null) }
                2 -> FloatingActionButton(onClick = viewModel::onAddHarvest)     { Icon(Icons.Filled.Add, null) }
                3 -> FloatingActionButton(onClick = viewModel::onAddTreatment)   { Icon(Icons.Filled.Add, null) }
                4 -> FloatingActionButton(onClick = viewModel::onAddFeeding)     { Icon(Icons.Filled.Add, null) }
                5 -> FloatingActionButton(onClick = viewModel::onAddTask)        { Icon(Icons.Filled.Add, null) }
                else -> {}
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        when (selectedTab) {
            0 -> HiveInfoTab(uiState, Modifier.padding(innerPadding))
            1 -> InspectionsTab(uiState.inspections, viewModel, Modifier.padding(innerPadding))
            2 -> HarvestsTab(uiState.harvests, viewModel, Modifier.padding(innerPadding))
            3 -> TreatmentsTab(uiState.treatments, viewModel, Modifier.padding(innerPadding))
            4 -> FeedingsTab(uiState.feedings, viewModel, Modifier.padding(innerPadding))
            5 -> TasksTab(uiState.tasks, viewModel, Modifier.padding(innerPadding))
        }
    }
}

// ─── Tab 0: Hive info ─────────────────────────────────────────────────────────

@Composable
private fun HiveInfoTab(uiState: HiveDetailUiState, modifier: Modifier = Modifier) {
    val hive = uiState.hive ?: return
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            InfoCard {
                InfoRow("Numer ula",        hive.number.toString())
                InfoRow("Status",           hive.status.displayName())
                InfoRow("Typ ramek",        hive.frameType)
                InfoRow("Liczba nadstawek", hive.superboxCount.toString())
                if (hive.queenYear != null) InfoRow("Rok matki", hive.queenYear.toString())
                if (hive.queenOrigin.isNotBlank()) InfoRow("Pochodzenie", hive.queenOrigin)
                InfoRow("Data instalacji",  hive.installedAt.format(dateFormatter))
            }
        }

        // Last inspection summary
        uiState.inspections.firstOrNull()?.let { last ->
            item {
                Text("Ostatni przegląd", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                InfoCard {
                    InfoRow("Data",             last.date.format(dateFormatter))
                    InfoRow("Siła rodziny",     last.colonyStrength.displayName())
                    InfoRow("Matka widoczna",   if (last.queenSeen) "Tak" else "Nie")
                    InfoRow("Czerw widoczny",   if (last.broodSeen) "Tak" else "Nie")
                    InfoRow("Mateczniki",       if (last.queenCellsSeen) "Tak" else "Nie")
                    if (last.notes.isNotBlank()) InfoRow("Notatki", last.notes)
                }
            }
        }

        if (hive.notes.isNotBlank()) {
            item {
                Text("Notatki", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(hive.notes, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) { content() }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// ─── Tab 1: Inspections ───────────────────────────────────────────────────────

@Composable
private fun InspectionsTab(
    inspections: List<Inspection>,
    viewModel: HiveDetailViewModel,
    modifier: Modifier = Modifier
) {
    if (inspections.isEmpty()) {
        EmptyTabMessage("Brak przeglądów. Dodaj pierwszy przegląd.", modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(inspections, key = { it.id }) { inspection ->
            ActivityCard(
                title    = inspection.date.format(dateFormatter),
                subtitle = "${inspection.colonyStrength.displayName()} • ${if (inspection.queenSeen) "Matka ✓" else "Matka ✗"}",
                body     = inspection.notes.ifBlank { null },
                onEdit   = { viewModel.onEditInspection(inspection.id) },
                onDelete = { viewModel.onDeleteInspection(inspection.id) }
            )
        }
    }
}

// ─── Tab 2: Harvests ──────────────────────────────────────────────────────────

@Composable
private fun HarvestsTab(
    harvests: List<HoneyHarvest>,
    viewModel: HiveDetailViewModel,
    modifier: Modifier = Modifier
) {
    if (harvests.isEmpty()) {
        EmptyTabMessage("Brak miodobrań. Dodaj pierwsze miodobranie.", modifier)
        return
    }
    val total = harvests.sumOf { it.weightKg.toDouble() }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            SummaryCard("W tym roku zebrano: ${"%.1f".format(total)} kg")
        }
        items(harvests, key = { it.id }) { harvest ->
            ActivityCard(
                title    = harvest.date.format(dateFormatter),
                subtitle = "${harvest.honeyType} • ${"%.1f".format(harvest.weightKg)} kg",
                body     = harvest.notes.ifBlank { null },
                onEdit   = { viewModel.onEditHarvest(harvest.id) },
                onDelete = { viewModel.onDeleteHarvest(harvest.id) }
            )
        }
    }
}

// ─── Tab 3: Treatments ────────────────────────────────────────────────────────

@Composable
private fun TreatmentsTab(
    treatments: List<Treatment>,
    viewModel: HiveDetailViewModel,
    modifier: Modifier = Modifier
) {
    if (treatments.isEmpty()) {
        EmptyTabMessage("Brak leczenia. Dodaj pierwsze leczenie.", modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(treatments, key = { it.id }) { treatment ->
            ActivityCard(
                title    = treatment.date.format(dateFormatter),
                subtitle = "${treatment.medicineType} • ${treatment.dosage}",
                body     = "Sposób: ${treatment.applicationMethod}".takeIf { treatment.applicationMethod.isNotBlank() },
                onEdit   = { viewModel.onEditTreatment(treatment.id) },
                onDelete = { viewModel.onDeleteTreatment(treatment.id) }
            )
        }
    }
}

// ─── Tab 4: Feedings ──────────────────────────────────────────────────────────

@Composable
private fun FeedingsTab(
    feedings: List<Feeding>,
    viewModel: HiveDetailViewModel,
    modifier: Modifier = Modifier
) {
    if (feedings.isEmpty()) {
        EmptyTabMessage("Brak dokarmiań. Dodaj pierwsze dokarmianie.", modifier)
        return
    }
    val total = feedings.sumOf { it.weightKg.toDouble() }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SummaryCard("W tym roku zakarmiono: ${"%.1f".format(total)} kg") }
        items(feedings, key = { it.id }) { feeding ->
            ActivityCard(
                title    = feeding.date.format(dateFormatter),
                subtitle = "${feeding.foodType} • ${"%.1f".format(feeding.weightKg)} kg",
                body     = feeding.applicationMethod.ifBlank { null },
                onEdit   = { viewModel.onEditFeeding(feeding.id) },
                onDelete = { viewModel.onDeleteFeeding(feeding.id) }
            )
        }
    }
}

// ─── Tab 5: Tasks ─────────────────────────────────────────────────────────────

@Composable
private fun TasksTab(
    tasks: List<Task>,
    viewModel: HiveDetailViewModel,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        EmptyTabMessage("Brak zadań dla tego ula.", modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tasks, key = { it.id }) { task ->
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.isCompleted, onCheckedChange = { viewModel.onTaskCheckedChange(task.id, it) })
                    Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        task.dueDate?.let { Text(it.format(dateFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    IconButton(onClick = { viewModel.onEditTask(task.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edytuj", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ─── Shared components ────────────────────────────────────────────────────────

@Composable
private fun ActivityCard(
    title: String,
    subtitle: String,
    body: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                body?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edytuj", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun SummaryCard(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text     = text,
            modifier = Modifier.padding(12.dp),
            style    = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color    = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyTabMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}
