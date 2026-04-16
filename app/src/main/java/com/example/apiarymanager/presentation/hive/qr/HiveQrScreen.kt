package com.example.apiarymanager.presentation.hive.qr

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveQrScreen(
    onNavigateBack: () -> Unit,
    viewModel: HiveQrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HiveQrEvent.NavigateBack ->
                    onNavigateBack()
                is HiveQrEvent.ShowMessage ->
                    snackbarHostState.showSnackbar(event.message)
                is HiveQrEvent.SendEmail ->
                    sendQrByEmail(context, event.bitmap, event.hiveName)
            }
        }
    }

    // Confirm regenerate dialog
    if (uiState.showRegenerateConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onRegenerateCancelled,
            title   = { Text("Nowy kod QR") },
            text    = { Text("Stary kod QR przestanie działać. Czy na pewno chcesz wygenerować nowy kod?") },
            confirmButton = {
                TextButton(onClick = viewModel::onRegenerateConfirmed) {
                    Text("Wygeneruj", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onRegenerateCancelled) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR kod ula") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment  = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.qrBitmap == null -> Box(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment  = Alignment.Center
            ) {
                Text(
                    text      = "Nie można wczytać kodu QR",
                    style     = MaterialTheme.typography.bodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            else -> Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.hiveName.isNotBlank()) {
                    Text(
                        text       = uiState.hiveName,
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (uiState.isRegenerating) {
                    Box(
                        modifier         = Modifier.fillMaxWidth().aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(modifier = Modifier.size(48.dp)) }
                } else {
                    Image(
                        bitmap             = uiState.qrBitmap!!.asImageBitmap(),
                        contentDescription = "Kod QR ula",
                        modifier           = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick  = viewModel::onSendEmailClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !uiState.isRegenerating
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Wyślij na email")
                }

                OutlinedButton(
                    onClick  = viewModel::onRegenerateRequest,
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !uiState.isRegenerating
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Wygeneruj nowy kod QR")
                }
            }
        }
    }
}

private fun sendQrByEmail(context: android.content.Context, bitmap: Bitmap, hiveName: String) {
    runCatching {
        // Save bitmap to cache/qr/ (must match file_provider_paths.xml)
        val qrDir = File(context.cacheDir, "qr").also { it.mkdirs() }
        val file = File(qrDir, "qr_${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type    = "image/png"
            putExtra(Intent.EXTRA_SUBJECT, "Kod QR ula: $hiveName")
            putExtra(Intent.EXTRA_TEXT, "W załączniku znajduje się kod QR dla ula: $hiveName")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Wyślij kod QR"))
    }
}
