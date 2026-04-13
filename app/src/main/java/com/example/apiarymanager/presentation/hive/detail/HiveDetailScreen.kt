package com.example.apiarymanager.presentation.hive.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Placeholder — will be fully implemented in Etap 4
@Composable
fun HiveDetailScreen(
    hiveId: Long,
    onNavigateToInspectionForm: (hiveId: Long, inspectionId: Long?) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("HiveDetail — hiveId=$hiveId — Etap 4")
    }
}
