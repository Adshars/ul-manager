package com.example.apiarymanager.presentation.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.core.security.BiometricHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToChangePin: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val versionName = remember {
        @Suppress("DEPRECATION")
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            info.versionName ?: "—"
        } catch (e: Exception) {
            "—"
        }
    }

    LaunchedEffect(Unit) {
        val available = BiometricHelper.isAvailable(context)
        viewModel.onBiometricAvailabilityChanged(available)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.NavigateToLogin    -> onNavigateToLogin()
                SettingsEvent.NavigateToChangePin -> onNavigateToChangePin()
                is SettingsEvent.ShowMessage     -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
                .padding(16.dp)
        ) {
            SectionTitle("Bezpieczeństwo")
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.isBiometricAvailable) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text("Logowanie biometrią", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("Używaj odcisku palca zamiast PINu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = uiState.isBiometricEnabled, onCheckedChange = viewModel::onBiometricToggle)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    }
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Lock, null, modifier = Modifier.size(24.dp))
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text("Zmień PIN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Zmień 4-cyfrowy PIN dostępu do aplikacji", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedButton(onClick = viewModel::onChangePinClick) { Text("Zmień") }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle("Wygląd")
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier          = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.DarkMode, null, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("Ciemny motyw", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Przełącz między jasnym a ciemnym motywem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = uiState.isDarkMode, onCheckedChange = viewModel::onDarkModeToggle)
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionTitle("Konto")
            Button(
                onClick  = viewModel::onLogout,
                enabled  = !uiState.isLoggingOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                if (uiState.isLoggingOut) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(24.dp)
                    )
                } else {
                    Icon(Icons.Filled.ExitToApp, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Wyloguj się", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                text     = "Wersja $versionName",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelLarge,
        color      = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(bottom = 8.dp)
    )
}
