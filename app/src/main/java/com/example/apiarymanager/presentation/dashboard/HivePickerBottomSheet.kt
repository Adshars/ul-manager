package com.example.apiarymanager.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apiarymanager.domain.model.Apiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivePickerBottomSheet(
    pickerState: HivePickerState,
    apiaries: List<DashboardApiary>,
    onApiarySelected: (Apiary) -> Unit,
    onHiveSelected: (Long) -> Unit,
    onBackToApiaries: () -> Unit,
    onScanQr: () -> Unit,
    onScanCancelled: () -> Unit,
    onQrScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        when {
            pickerState.isScanning -> ScannerStep(
                onQrScanned     = onQrScanned,
                onScanCancelled = onScanCancelled
            )
            else -> PickerStep(
                pickerState      = pickerState,
                apiaries         = apiaries,
                onApiarySelected = onApiarySelected,
                onHiveSelected   = onHiveSelected,
                onBackToApiaries = onBackToApiaries,
                onScanQr         = onScanQr
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PickerStep(
    pickerState: HivePickerState,
    apiaries: List<DashboardApiary>,
    onApiarySelected: (Apiary) -> Unit,
    onHiveSelected: (Long) -> Unit,
    onBackToApiaries: () -> Unit,
    onScanQr: () -> Unit
) {
    // ── Header ────────────────────────────────────────────────────────────────
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pickerState.selectedApiary != null) {
            IconButton(onClick = onBackToApiaries) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć do listy pasiek")
            }
        } else {
            Spacer(Modifier.width(16.dp))
        }
        Text(
            text       = if (pickerState.selectedApiary == null) "Wybierz pasiekę" else "Wybierz ul",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.weight(1f)
        )
        // QR scan shortcut visible only on apiary-selection step
        if (pickerState.selectedApiary == null) {
            IconButton(onClick = onScanQr) {
                Icon(
                    imageVector        = Icons.Filled.QrCodeScanner,
                    contentDescription = "Skanuj QR ula",
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (pickerState.selectedApiary != null) {
        Text(
            text     = pickerState.selectedApiary.name,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )
    }

    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

    // ── Content ───────────────────────────────────────────────────────────────
    if (pickerState.selectedApiary == null) {
        LazyColumn {
            items(apiaries, key = { it.apiary.id }) { dashApiary ->
                ListItem(
                    headlineContent = {
                        Text(dashApiary.apiary.name, fontWeight = FontWeight.Medium)
                    },
                    supportingContent = {
                        Text(
                            text = buildString {
                                if (dashApiary.apiary.location.isNotBlank()) append(dashApiary.apiary.location)
                                if (dashApiary.activeHiveCount > 0) {
                                    if (isNotEmpty()) append(" · ")
                                    append("${dashApiary.activeHiveCount} aktywne ule")
                                }
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector        = Icons.Filled.Hive,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.clickable { onApiarySelected(dashApiary.apiary) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    } else {
        if (pickerState.isLoadingHives) {
            Box(
                modifier            = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment    = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (pickerState.hives.isEmpty()) {
            Box(
                modifier            = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text  = "Brak uli w tej pasiece",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(pickerState.hives, key = { it.id }) { hive ->
                    ListItem(
                        headlineContent = {
                            Text("Ul #${hive.number} — ${hive.name}", fontWeight = FontWeight.Medium)
                        },
                        supportingContent = {
                            Text(
                                text  = hive.status.displayName(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector        = Icons.Filled.Hive,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.secondary,
                                modifier           = Modifier.size(24.dp)
                            )
                        },
                        modifier = Modifier.clickable { onHiveSelected(hive.id) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun ScannerStep(
    onQrScanned: (String) -> Unit,
    onScanCancelled: () -> Unit
) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onScanCancelled) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anuluj skanowanie")
            }
            Text(
                text       = "Zeskanuj kod QR ula",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.weight(1f)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        QrScannerView(
            onScanned = onQrScanned,
            modifier  = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier         = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onScanCancelled) { Text("Anuluj") }
        }
    }
}
