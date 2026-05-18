package com.example.apiarymanager.presentation.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.core.security.BiometricHelper
import com.example.apiarymanager.presentation.components.PinDots
import com.example.apiarymanager.presentation.components.PinKeypad

@Composable
fun PinUnlockScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: PinUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val triggerBiometric = {
        activity?.let {
            BiometricHelper.authenticate(
                activity           = it,
                title              = "Odblokuj aplikację",
                subtitle           = "Użyj biometrii, aby się zalogować",
                negativeButtonText = "Użyj PINu",
                onSuccess          = viewModel::onBiometricSuccess,
                onError            = { viewModel.onBiometricCancelled() },
                onFailed           = viewModel::onBiometricFailed,
                onCancelled        = viewModel::onBiometricCancelled
            )
        }
        Unit
    }

    LaunchedEffect(Unit) {
        val available = BiometricHelper.isAvailable(context)
        viewModel.onBiometricAvailabilityChanged(available)
    }

    LaunchedEffect(uiState.isInBiometricMode) {
        if (!uiState.isInBiometricMode) {
            BiometricHelper.cancelAuthentication()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PinUnlockEvent.NavigateToDashboard -> onNavigateToDashboard()
                PinUnlockEvent.NavigateToLogin     -> onNavigateToLogin()
                PinUnlockEvent.TriggerBiometric    -> triggerBiometric()
                is PinUnlockEvent.ShowMessage       -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (uiState.showForgotPinDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissForgotPinDialog,
            title = { Text("Zresetuj PIN") },
            text  = {
                Text("Nastąpi wylogowanie z konta Microsoft. Po ponownym zalogowaniu będziesz mógł ustawić nowy PIN.")
            },
            confirmButton = {
                Button(onClick = viewModel::onForgotPinConfirmed) {
                    Text("Kontynuuj")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::onDismissForgotPinDialog) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        if (uiState.isInBiometricMode) {
            BiometricUnlockContent(
                uiState        = uiState,
                onFingerprintClick = triggerBiometric,
                onUsePinClick  = viewModel::onBiometricCancelled,
                onForgotPin    = viewModel::onForgotPinClick,
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
            )
        } else {
            PinUnlockContent(
                uiState        = uiState,
                onDigit        = viewModel::onDigitEntered,
                onBackspace    = viewModel::onBackspace,
                onForgotPin    = viewModel::onForgotPinClick,
                isLoggingOut   = uiState.isLoggingOut,
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun BiometricUnlockContent(
    uiState: PinUnlockUiState,
    onFingerprintClick: () -> Unit,
    onUsePinClick: () -> Unit,
    onForgotPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(48.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "Witaj z powrotem",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Dotknij czytnika, aby odblokować aplikację",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = uiState.error,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }

        IconButton(
            onClick  = onFingerprintClick,
            modifier = Modifier.size(96.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Fingerprint,
                contentDescription = "Zaloguj odciskiem palca",
                modifier           = Modifier.size(80.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextButton(onClick = onUsePinClick) {
                Text("Użyj PINu")
            }
            TextButton(onClick = onForgotPin) {
                Text("Nie pamiętasz PINu?")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PinUnlockContent(
    uiState: PinUnlockUiState,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onForgotPin: () -> Unit,
    isLoggingOut: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(48.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "Witaj z powrotem",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text      = "Wprowadź PIN, aby odblokować aplikację",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (uiState.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = uiState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        PinDots(filled = uiState.pin.length)

        PinKeypad(
            onDigit     = onDigit,
            onBackspace = onBackspace
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoggingOut) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(onClick = onForgotPin) {
                    Text("Nie pamiętasz PINu?")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
