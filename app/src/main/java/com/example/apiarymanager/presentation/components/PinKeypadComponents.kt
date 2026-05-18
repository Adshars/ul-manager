package com.example.apiarymanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun PinDots(filled: Int, total: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < filled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(
        modifier              = modifier,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Box(Modifier.size(80.dp))
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
                            androidx.compose.material3.Text(
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
