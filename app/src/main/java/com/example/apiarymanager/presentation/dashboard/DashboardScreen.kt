package com.example.apiarymanager.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.Apiary
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.TaskPriority
import com.example.apiarymanager.presentation.theme.ApiaryManagerTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── Screen entry point ───────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    onNavigateToHiveList: (apiaryId: Long) -> Unit,
    onNavigateToApiaryForm: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateToHiveList -> onNavigateToHiveList(event.apiaryId)
                is DashboardEvent.ShowMessage        -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { DashboardTopBar(onOpenDrawer = onOpenDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToApiaryForm) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj pasiekę")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DashboardContent(
                uiState             = uiState,
                onApiaryClick       = viewModel::onApiaryClick,
                onTaskCheckedChange = viewModel::onTaskCheckedChange,
                onQuickActionClick  = viewModel::onQuickActionClick,
                modifier            = Modifier.padding(innerPadding)
            )
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onOpenDrawer: () -> Unit = {}) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector     = Icons.Filled.Hive,
                    contentDescription = null,
                    tint            = MaterialTheme.colorScheme.primary,
                    modifier        = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "ApiaryManager",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector     = Icons.Outlined.AccountCircle,
                    contentDescription = "Profil",
                    tint            = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ─── Main content ─────────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onApiaryClick: (Long) -> Unit,
    onTaskCheckedChange: (Long, Boolean) -> Unit,
    onQuickActionClick: (QuickActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier        = modifier.fillMaxSize(),
        contentPadding  = PaddingValues(bottom = 32.dp)
    ) {
        // ── Greeting ──────────────────────────────────────────────────────────
        item { GreetingHeader() }

        // ── Quick actions ─────────────────────────────────────────────────────
        item {
            SectionHeader(title = "Szybkie akcje")
            QuickActionsRow(onActionClick = onQuickActionClick)
            Spacer(Modifier.height(8.dp))
        }

        // ── Apiaries ──────────────────────────────────────────────────────────
        item {
            SectionHeader(
                title = "Moje pasieki",
                badge = uiState.apiaries.size.takeIf { it > 0 }
            )
        }
        if (uiState.apiaries.isEmpty()) {
            item { EmptyState(message = "Brak pasiek. Dodaj pierwszą pasiekę.") }
        } else {
            items(items = uiState.apiaries, key = { "apiary_${it.apiary.id}" }) { dashApiary ->
                ApiaryCard(
                    dashApiary = dashApiary,
                    onClick    = { onApiaryClick(dashApiary.apiary.id) },
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // ── Pending tasks ─────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            SectionHeader(
                title = "Zadania do wykonania",
                badge = uiState.pendingTasks.size.takeIf { it > 0 }
            )
        }
        if (uiState.pendingTasks.isEmpty()) {
            item { EmptyState(message = "Brak zaległych zadań. Dobra robota!") }
        } else {
            items(items = uiState.pendingTasks, key = { "task_${it.id}" }) { task ->
                TaskItem(
                    task           = task,
                    onCheckedChange = { onTaskCheckedChange(task.id, it) },
                    modifier       = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ─── Greeting header ──────────────────────────────────────────────────────────

@Composable
private fun GreetingHeader(modifier: Modifier = Modifier) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11  -> "Dzień dobry!"
        in 12..17 -> "Miłego popołudnia!"
        else      -> "Dobry wieczór!"
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            text       = greeting,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "Sprawdź co czeka na Twoje pasieki",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    badge: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )
        if (badge != null) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(text = badge.toString(), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ─── Quick actions ────────────────────────────────────────────────────────────

private data class QuickActionItem(
    val icon: ImageVector,
    val label: String,
    val type: QuickActionType
)

private val quickActionItems = listOf(
    QuickActionItem(Icons.Outlined.ContentPaste, "Nowy\nprzegląd", QuickActionType.NEW_INSPECTION),
    QuickActionItem(Icons.Outlined.WaterDrop,    "Miodo-\nbranie",  QuickActionType.HARVEST),
    QuickActionItem(Icons.Outlined.PlaylistAdd,  "Dodaj\nzadanie",  QuickActionType.ADD_TASK),
    QuickActionItem(Icons.Outlined.Map,          "Mapa\npasiek",    QuickActionType.MAP)
)

@Composable
private fun QuickActionsRow(
    onActionClick: (QuickActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier            = modifier,
        contentPadding      = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(quickActionItems) { item ->
            QuickActionCard(
                icon    = item.icon,
                label   = item.label,
                onClick = { onActionClick(item.type) }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick  = onClick,
        modifier = modifier.size(width = 100.dp, height = 88.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector     = icon,
                contentDescription = label,
                tint            = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier        = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelSmall.copy(lineHeight = 15.sp),
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ─── Apiary card ──────────────────────────────────────────────────────────────

@Composable
private fun ApiaryCard(
    dashApiary: DashboardApiary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular icon container
            Box(
                modifier          = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment  = Alignment.Center
            ) {
                Icon(
                    imageVector     = Icons.Filled.Hive,
                    contentDescription = null,
                    tint            = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier        = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = dashApiary.apiary.name,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector     = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint            = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier        = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text     = dashApiary.apiary.location,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (dashApiary.activeHiveCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "${dashApiary.activeHiveCount} aktywne ule",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector     = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Otwórz",
                tint            = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Task item ────────────────────────────────────────────────────────────────

private val polishDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("pl"))

@Composable
private fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked         = task.isCompleted,
                onCheckedChange = onCheckedChange
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                Text(
                    text       = task.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    task.dueDate?.let { date ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector     = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint            = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier        = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text  = date.format(polishDateFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (date.isBefore(LocalDate.now()))
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PriorityBadge(priority = task.priority)
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority) {
        TaskPriority.HIGH   ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        TaskPriority.MEDIUM ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TaskPriority.LOW    ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text  = priority.displayName(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    ApiaryManagerTheme {
        DashboardContent(
            uiState = DashboardUiState(
                isLoading = false,
                apiaries = listOf(
                    DashboardApiary(
                        apiary = Apiary(1, "Pasieka Leśna", "Bory Tucholskie",
                            createdAt = LocalDate.of(2022, 4, 15)),
                        activeHiveCount = 3
                    ),
                    DashboardApiary(
                        apiary = Apiary(2, "Pasieka Ogrodowa", "Gdańsk-Oliwa",
                            createdAt = LocalDate.of(2023, 3, 20)),
                        activeHiveCount = 2
                    )
                ),
                pendingTasks = listOf(
                    Task(1, title = "Odbiór miodu — Pasieka Leśna",
                        priority = TaskPriority.HIGH,
                        dueDate = LocalDate.of(2024, 6, 10)),
                    Task(2, title = "Wymiana matki — ul Gamma",
                        priority = TaskPriority.HIGH,
                        dueDate = LocalDate.of(2024, 6, 1))
                )
            ),
            onApiaryClick       = {},
            onTaskCheckedChange = { _, _ -> },
            onQuickActionClick  = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionCardPreview() {
    ApiaryManagerTheme {
        QuickActionCard(
            icon    = Icons.Outlined.ContentPaste,
            label   = "Nowy\nprzegląd",
            onClick = {}
        )
    }
}
