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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        icon        = Icons.Outlined.ContentPaste,
        title       = "Pełna historia ula",
        description = "Zapisuj przeglądy, miodobrania, dokarmiania i leczenia. Zawsze wiesz, co dzieje się w każdym ulu."
    ),
    OnboardingPage(
        icon        = Icons.Outlined.Map,
        title       = "Mapa i pogoda",
        description = "Wizualizuj lokalizacje swoich pasiek na mapie i sprawdzaj pogodę dla każdej z nich."
    ),
    OnboardingPage(
        icon        = Icons.Outlined.Mic,
        title       = "Głosowe notatki",
        description = "Rób szybkie notatki głosowe podczas pracy przy ulach — bez brudzenia telefonu."
    ),
    OnboardingPage(
        icon        = Icons.Outlined.WaterDrop,
        title       = "Statystyki i wykresy",
        description = "Analizuj zbiory miodu, dokarmiania i siłę rodzin pszczelych za pomocą czytelnych wykresów."
    )
)

@Composable
fun OnboardingCarouselScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState { pages.size }
    val scope      = rememberCoroutineScope()

    Column(
        modifier              = Modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // Dots
        Row(
            modifier              = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(pages.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Navigation buttons
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < pages.lastIndex) {
                TextButton(onClick = onFinish) { Text("Pomiń") }
                Button(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }) { Text("Dalej") }
            } else {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick  = onFinish,
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) { Text("Zaczynamy!") }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier              = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Box(
            modifier         = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector     = page.icon,
                contentDescription = null,
                tint            = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier        = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = page.description,
            style     = MaterialTheme.typography.bodyLarge,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
