package com.example.apiarymanager.presentation.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Fingerprint
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePinScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangePinViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    LaunchedEffect(Unit) {
        val available = BiometricHelper.isAvailable(context)
        viewModel.onBiometricAvailabilityChanged(available)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ChangePinEvent.NavigateBack     -> onNavigateBack()
                ChangePinEvent.TriggerBiometric -> activity?.let {
                    BiometricHelper.authenticate(
                        activity           = it,
                        title              = "Zmień PIN",
                        subtitle           = "Potwierdź tożsamość, aby zmienić PIN",
                        negativeButtonText = "Użyj PINu",
                        onSuccess          = viewModel::onBiometricSuccess,
                        onError            = {}
                    )
                }
                is ChangePinEvent.ShowMessage -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zmień PIN") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = when (uiState.step) {
                        ChangePinStep.VERIFY_CURRENT -> "Potwierdź tożsamość"
                        ChangePinStep.ENTER_NEW      -> "Nowy PIN"
                        ChangePinStep.CONFIRM_NEW    -> "Potwierdź nowy PIN"
                    },
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = when (uiState.step) {
                        ChangePinStep.VERIFY_CURRENT -> "Wprowadź aktualny PIN"
                        ChangePinStep.ENTER_NEW      -> "Wprowadź nowy 4-cyfrowy PIN"
                        ChangePinStep.CONFIRM_NEW    -> "Wprowadź nowy PIN ponownie"
                    },
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (uiState.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = uiState.error!!,
                        style     = MaterialTheme.typography.bodySmall,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            PinDots(
                filled = when (uiState.step) {
                    ChangePinStep.VERIFY_CURRENT -> uiState.pin.length
                    ChangePinStep.ENTER_NEW      -> uiState.newPin.length
                    ChangePinStep.CONFIRM_NEW    -> uiState.confirmPin.length
                }
            )

            PinKeypad(
                onDigit     = viewModel::onDigitEntered,
                onBackspace = viewModel::onBackspace
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.step == ChangePinStep.VERIFY_CURRENT &&
                    uiState.isBiometricAvailable && uiState.isBiometricEnabled) {
                    IconButton(
                        onClick = {
                            activity?.let {
                                BiometricHelper.authenticate(
                                    activity           = it,
                                    title              = "Zmień PIN",
                                    subtitle           = "Potwierdź tożsamość, aby zmienić PIN",
                                    negativeButtonText = "Użyj PINu",
                                    onSuccess          = viewModel::onBiometricSuccess,
                                    onError            = {}
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Outlined.Fingerprint,
                            contentDescription = "Potwierdź biometrią",
                            modifier           = Modifier.size(40.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
