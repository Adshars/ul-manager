package com.example.apiarymanager.presentation.task

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.Task
import com.example.apiarymanager.domain.model.TaskPriority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val polishLocale = Locale("pl")
private val monthYearFmt = DateTimeFormatter.ofPattern("LLLL yyyy", polishLocale)
private val dayMonthFmt  = DateTimeFormatter.ofPattern("d MMMM", polishLocale)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToTaskForm: (taskId: Long?) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskListEvent.NavigateToTaskForm -> onNavigateToTaskForm(event.taskId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                },
                title = { Text("Zadania", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddTaskClick) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj zadanie")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Przełącznik widoku ─────────────────────────────────────────────
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = uiState.activeView == TaskListView.LIST,
                    onClick  = { viewModel.onViewToggle(TaskListView.LIST) },
                    shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon     = { Icon(Icons.Outlined.FormatListBulleted, contentDescription = null, modifier = Modifier.size(18.dp)) }
                ) { Text("Lista") }
                SegmentedButton(
                    selected = uiState.activeView == TaskListView.CALENDAR,
                    onClick  = { viewModel.onViewToggle(TaskListView.CALENDAR) },
                    shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon     = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                ) { Text("Kalendarz") }
            }

            if (uiState.activeView == TaskListView.LIST) {
                TaskListContent(
                    uiState             = uiState,
                    onFilterChange      = viewModel::onFilterChange,
                    onTaskCheckedChange = viewModel::onTaskCheckedChange,
                    onTaskClick         = viewModel::onTaskClick
                )
            } else {
                CalendarContent(
                    uiState             = uiState,
                    onPreviousMonth     = viewModel::onPreviousMonth,
                    onNextMonth         = viewModel::onNextMonth,
                    onDateSelected      = viewModel::onDateSelected,
                    onTaskCheckedChange = viewModel::onTaskCheckedChange,
                    onTaskClick         = viewModel::onTaskClick
                )
            }
        }
    }
}

// ─── Widok listy ──────────────────────────────────────────────────────────────

@Composable
private fun TaskListContent(
    uiState: TaskListUiState,
    onFilterChange: (TaskListFilter) -> Unit,
    onTaskCheckedChange: (Long, Boolean) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                TaskListFilter.ALL       to "Wszystkie",
                TaskListFilter.PENDING   to "Aktywne",
                TaskListFilter.COMPLETED to "Ukończone"
            ).forEach { (filter, label) ->
                FilterChip(
                    selected = uiState.activeFilter == filter,
                    onClick  = { onFilterChange(filter) },
                    label    = { Text(label) }
                )
            }
        }

        val grouped = uiState.groupedTasks()

        if (grouped.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "Brak zadań w tej kategorii",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                grouped.forEach { (group, tasks) ->
                    item(key = "header_${group.name}") {
                        GroupHeader(group = group)
                    }
                    items(tasks, key = { "task_${it.id}" }) { task ->
                        TaskListItem(
                            task            = task,
                            today           = uiState.today,
                            onCheckedChange = { onTaskCheckedChange(task.id, it) },
                            onClick         = { onTaskClick(task.id) }
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    item(key = "spacer_${group.name}") { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: TaskGroup, modifier: Modifier = Modifier) {
    val color = when (group) {
        TaskGroup.OVERDUE  -> MaterialTheme.colorScheme.error
        TaskGroup.TODAY    -> MaterialTheme.colorScheme.primary
        TaskGroup.UPCOMING -> MaterialTheme.colorScheme.secondary
        TaskGroup.DONE     -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text     = group.label.uppercase(),
        style    = MaterialTheme.typography.labelMedium,
        color    = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun TaskListItem(
    task: Task,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier.padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.isCompleted, onCheckedChange = onCheckedChange)
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text            = task.title,
                    style           = MaterialTheme.typography.bodyMedium,
                    fontWeight      = FontWeight.Medium,
                    maxLines        = 2,
                    overflow        = TextOverflow.Ellipsis,
                    textDecoration  = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color           = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                      else MaterialTheme.colorScheme.onSurface
                )
                task.dueDate?.let { date ->
                    val dateColor = when {
                        task.isCompleted     -> MaterialTheme.colorScheme.onSurfaceVariant
                        date.isBefore(today) -> MaterialTheme.colorScheme.error
                        date == today        -> MaterialTheme.colorScheme.primary
                        else                 -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text  = date.format(dayMonthFmt),
                        style = MaterialTheme.typography.labelSmall,
                        color = dateColor
                    )
                }
            }
            PriorityDot(task.priority)
        }
    }
}

// ─── Widok kalendarza ─────────────────────────────────────────────────────────

@Composable
private fun CalendarContent(
    uiState: TaskListUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTaskCheckedChange: (Long, Boolean) -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val datesWithTasks = uiState.datesWithTasks()
    val tasksForDay    = uiState.tasksForSelectedDate()

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            MonthCalendar(
                month          = uiState.calendarMonth,
                today          = uiState.today,
                selectedDate   = uiState.selectedDate,
                datesWithTasks = datesWithTasks,
                onPrevious     = onPreviousMonth,
                onNext         = onNextMonth,
                onDayClick     = onDateSelected
            )
        }

        // Nagłówek wybranego dnia
        item {
            Text(
                text     = uiState.selectedDate.format(dayMonthFmt)
                    .replaceFirstChar { it.uppercase() },
                style    = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color    = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (tasksForDay.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "Brak zadań w tym dniu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(tasksForDay, key = { "cal_task_${it.id}" }) { task ->
                TaskListItem(
                    task            = task,
                    today           = uiState.today,
                    onCheckedChange = { onTaskCheckedChange(task.id, it) },
                    onClick         = { onTaskClick(task.id) },
                    modifier        = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        item { Spacer(Modifier.height(80.dp)) } // padding pod FAB
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    today: LocalDate,
    selectedDate: LocalDate,
    datesWithTasks: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Nawigacja miesiąca
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Poprzedni miesiąc")
            }
            Text(
                text      = month.format(monthYearFmt).replaceFirstChar { it.uppercase() },
                style     = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier  = Modifier.weight(1f)
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Następny miesiąc")
            }
        }

        // Nagłówki dni tygodnia (Pn–Nd)
        val dayHeaders = (1..7).map {
            DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, polishLocale)
                .replaceFirstChar { c -> c.uppercase() }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { label ->
                Text(
                    text      = label,
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.weight(1f).padding(vertical = 4.dp)
                )
            }
        }

        // Siatkа dni
        val firstDay       = month.atDay(1)
        val offsetDays     = (firstDay.dayOfWeek.value - 1) // 0 = pn, 6 = nd
        val daysInMonth    = month.lengthOfMonth()
        val totalCells     = offsetDays + daysInMonth
        val rows           = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - offsetDays + 1
                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date        = month.atDay(dayNumber)
                        val isSelected  = date == selectedDate
                        val isToday     = date == today
                        val hasTasks    = date in datesWithTasks

                        Box(
                            modifier         = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday    -> MaterialTheme.colorScheme.primaryContainer
                                        else       -> Color.Transparent
                                    }
                                )
                                .then(
                                    if (isToday && !isSelected)
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    else Modifier
                                )
                                .clickable { onDayClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text  = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday    -> MaterialTheme.colorScheme.primary
                                        else       -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasTasks) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun PriorityDot(priority: TaskPriority, modifier: Modifier = Modifier) {
    val color = when (priority) {
        TaskPriority.HIGH   -> MaterialTheme.colorScheme.error
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.secondary
        TaskPriority.LOW    -> MaterialTheme.colorScheme.surfaceVariant
    }
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}
