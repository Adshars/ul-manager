package com.example.apiarymanager.presentation.inspection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Placeholder — will be fully implemented in Etap 5
@Composable
fun InspectionFormScreen(
    hiveId: Long,
    inspectionId: Long?,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val mode = if (inspectionId == null) "Nowy" else "Edycja #$inspectionId"
        Text("InspectionForm — $mode — hiveId=$hiveId — Etap 5")
    }
}
