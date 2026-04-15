package com.example.apiarymanager.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.core.security.BiometricHelper

@Composable
fun PinScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: PinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Check biometric availability
    LaunchedEffect(Unit) {
        val available = (context as? FragmentActivity)?.let { BiometricHelper.isAvailable(it) } ?: false
        viewModel.onBiometricAvailabilityChanged(available)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PinEvent.NavigateToDashboard       -> onNavigateToDashboard()
                is PinEvent.ShowMessage            -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(40.dp))

            // Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = when (uiState.step) {
                        PinStep.ENTER   -> "Ustaw PIN"
                        PinStep.CONFIRM -> "Potwierdź PIN"
                    },
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = when (uiState.step) {
                        PinStep.ENTER   -> "Wprowadź 4-cyfrowy PIN do aplikacji"
                        PinStep.CONFIRM -> "Wprowadź PIN ponownie, aby potwierdzić"
                    },
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                uiState.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // PIN dots
            val currentPin = when (uiState.step) {
                PinStep.ENTER   -> uiState.pin
                PinStep.CONFIRM -> uiState.confirmPin
            }
            PinDots(filled = currentPin.length)

            // Keypad
            PinKeypad(
                onDigit     = viewModel::onDigitEntered,
                onBackspace = viewModel::onBackspace
            )

            // Biometric toggle (only after entering PIN)
            if (uiState.step == PinStep.CONFIRM && uiState.isBiometricAvailable) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector     = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        modifier        = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text("Logowanie biometrią", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Użyj odcisku palca zamiast PINu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked         = uiState.isBiometricEnabled,
                        onCheckedChange = viewModel::onBiometricToggle
                    )
                }
            }

            // Bottom actions
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.step == PinStep.CONFIRM) {
                    TextButton(onClick = viewModel::onResetStep) {
                        Text("Zmień PIN")
                    }
                }
                TextButton(onClick = viewModel::onSkip) {
                    Text("Pomiń — nie ustawiaj PINu")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PinDots(filled: Int, total: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(total) { index ->
            val isFilled = index < filled
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column(
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Spacer(Modifier.size(80.dp))
                        key == "⌫"   -> IconButton(
                            onClick  = onBackspace,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Filled.Backspace,
                                contentDescription = "Usuń",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        else -> FilledTonalButton(
                            onClick  = { onDigit(key) },
                            modifier = Modifier.size(80.dp),
                            shape    = CircleShape
                        ) {
                            Text(
                                text  = key,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
