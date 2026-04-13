package com.example.apiarymanager.presentation.hive.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Placeholder — will be fully implemented in Etap 4
@Composable
fun HiveListScreen(
    apiaryId: Long,
    onNavigateToHiveDetail: (hiveId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("HiveList — apiaryId=$apiaryId — Etap 4")
    }
}
