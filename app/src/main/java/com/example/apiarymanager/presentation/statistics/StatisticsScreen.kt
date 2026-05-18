package com.example.apiarymanager.presentation.statistics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier            = Modifier.padding(innerPadding),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Filters
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ApiaryFilterDropdown(
                        apiaries   = uiState.apiaries,
                        selectedId = uiState.selectedApiaryId,
                        onSelect   = viewModel::onApiarySelected,
                        modifier   = Modifier.weight(2f)
                    )
                    if (uiState.availableYears.size > 1) {
                        YearFilterDropdown(
                            years        = uiState.availableYears,
                            selectedYear = uiState.selectedYear,
                            onSelect     = viewModel::onYearSelected,
                            modifier     = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Summary cards
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryCard(
                        label    = "Miód",
                        value    = "${"%.1f".format(uiState.totalHoneyKg)} kg",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label    = "Dokarmianie",
                        value    = "${"%.1f".format(uiState.totalFeedingKg)} kg",
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label    = "Ule",
                        value    = formatHiveCount(uiState.hiveCount),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monthly harvest chart
            if (uiState.monthlyHarvest.isNotEmpty()) {
                item {
                    ChartCard(title = "Miodobrania (kg)", data = uiState.monthlyHarvest)
                }
            }

            // Monthly feeding chart
            if (uiState.monthlyFeeding.isNotEmpty()) {
                item {
                    ChartCard(title = "Dokarmiania (kg)", data = uiState.monthlyFeeding)
                }
            }

            // Honey type breakdown
            if (uiState.harvestByHoneyType.isNotEmpty()) {
                item {
                    HoneyTypeCard(data = uiState.harvestByHoneyType)
                }
            }

            // Empty state
            if (uiState.monthlyHarvest.isEmpty() && uiState.monthlyFeeding.isEmpty()) {
                item {
                    Box(
                        modifier            = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment    = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Brak danych", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Dodaj miodobranie lub dokarmianie, aby zobaczyć statystyki",
                                style     = MaterialTheme.typography.bodySmall,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChartCard(title: String, data: Map<Int, Float>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        val values = data.values.map { it.toDouble() }
        modelProducer.runTransaction {
            columnSeries { series(values) }
        }
    }

    val monthLabels = data.keys.map {
        Month.of(it).getDisplayName(TextStyle.SHORT, Locale("pl"))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(),
                    startAxis  = VerticalAxis.rememberStart(),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = { _, value, _ ->
                            monthLabels.getOrElse(value.toInt()) { "" }
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier      = Modifier.height(220.dp)
            )
        }
    }
}

@Composable
private fun HoneyTypeCard(data: Map<String, Float>) {
    val total = data.values.sum().takeIf { it > 0f } ?: 1f
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Typy miodu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            data.entries.forEachIndexed { index, (type, kg) ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        type,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(88.dp)
                    )
                    LinearProgressIndicator(
                        progress = { kg / total },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "${"%.1f".format(kg)} kg",
                        style     = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        modifier  = Modifier.width(56.dp).padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiaryFilterDropdown(
    apiaries:   List<com.example.apiarymanager.domain.model.Apiary>,
    selectedId: Long?,
    onSelect:   (Long?) -> Unit,
    modifier:   Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = if (selectedId == null) "Wszystkie pasieki"
                else apiaries.firstOrNull { it.id == selectedId }?.name ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value         = label,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Pasieka") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Wszystkie") }, onClick = { onSelect(null); expanded = false })
            apiaries.forEach { a ->
                DropdownMenuItem(text = { Text(a.name) }, onClick = { onSelect(a.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearFilterDropdown(
    years:        List<Int>,
    selectedYear: Int?,
    onSelect:     (Int?) -> Unit,
    modifier:     Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedYear?.toString() ?: "Wszystkie"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value         = label,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Rok") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Wszystkie") }, onClick = { onSelect(null); expanded = false })
            years.forEach { year ->
                DropdownMenuItem(text = { Text(year.toString()) }, onClick = { onSelect(year); expanded = false })
            }
        }
    }
}

private fun formatHiveCount(count: Int): String = when (count) {
    1    -> "1 ul"
    in 2..4 -> "$count ule"
    else -> "$count uli"
}
