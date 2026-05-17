package com.example.apiarymanager.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.presentation.theme.ApiaryManagerTheme

@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToDashboard -> onNavigateToDashboard()
                LoginEvent.NavigateToRegister  -> onNavigateToRegister()
            }
        }
    }

    Scaffold { innerPadding ->
        LoginContent(
            uiState        = uiState,
            onLoginClick   = { viewModel.onLoginClick(activity) },
            onRegisterClick = viewModel::onRegisterClick,
            modifier       = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = Icons.Filled.Hive,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "UL Manager",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = "Zarządzaj swoją pasieką",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(48.dp))

        if (uiState.generalError != null) {
            Text(
                text      = uiState.generalError,
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick  = onLoginClick,
            enabled  = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.size(24.dp)
                )
            } else {
                Text("Zaloguj się kontem Microsoft", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text  = "Nie masz konta?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onRegisterClick) {
                Text("Zarejestruj się")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    ApiaryManagerTheme {
        LoginContent(
            uiState         = LoginUiState(),
            onLoginClick    = {},
            onRegisterClick = {}
        )
    }
}
