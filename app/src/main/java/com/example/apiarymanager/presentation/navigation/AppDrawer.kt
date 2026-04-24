package com.example.apiarymanager.presentation.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class DrawerItem(
    val icon: ImageVector,
    val label: String,
    val route: Any
)

private val drawerItems = listOf(
    DrawerItem(Icons.Filled.Home,          "Dashboard",  DashboardRoute),
    DrawerItem(Icons.Filled.Hive,          "Pasieki",    ApiaryListRoute),
    DrawerItem(Icons.Outlined.CheckCircle, "Zadania",    TaskListRoute),
    DrawerItem(Icons.Filled.BarChart,      "Statystyki", StatisticsRoute),
    DrawerItem(Icons.Filled.Settings,      "Ustawienia", SettingsRoute)
)

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onNavigate: (Any) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier.fillMaxHeight()) {
        Spacer(Modifier.height(24.dp))

        // Header
        Icon(
            imageVector        = Icons.Filled.Hive,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier
                .padding(horizontal = 28.dp)
                .size(40.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "ApiaryManager",
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 28.dp)
        )
        Text(
            text     = "Zarządzaj swoją pasieką",
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 28.dp)
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        drawerItems.forEach { item ->
            NavigationDrawerItem(
                icon     = { Icon(item.icon, contentDescription = item.label) },
                label    = { Text(item.label) },
                selected = currentRoute?.contains(item.route::class.qualifiedName ?: "") == true,
                onClick  = { onNavigate(item.route) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Spacer(Modifier.weight(1f))
        HorizontalDivider()
        NavigationDrawerItem(
            icon     = { Icon(Icons.Filled.ExitToApp, contentDescription = "Wyloguj się", tint = MaterialTheme.colorScheme.error) },
            label    = { Text("Wyloguj się", color = MaterialTheme.colorScheme.error) },
            selected = false,
            onClick  = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
