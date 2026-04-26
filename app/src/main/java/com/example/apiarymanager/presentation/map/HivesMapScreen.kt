package com.example.apiarymanager.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apiarymanager.domain.model.Hive
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val POLAND_LAT   = 52.0
private const val POLAND_LON   = 19.5
private const val POLAND_ZOOM  = 7.5   // Poland fills screen, neighbours not visible
private const val SINGLE_ZOOM  = 14.0  // Street-level for a single hive
private const val BOUNDS_PAD   = 150   // Pixel padding around bounding box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivesMapScreen(
    onNavigateToHiveDetail: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HivesMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa pasiek") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        AllHivesMapView(
            hives                  = uiState.hives.filter { it.latitude != null && it.longitude != null },
            onNavigateToHiveDetail = onNavigateToHiveDetail,
            modifier               = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}

@Composable
private fun AllHivesMapView(
    hives: List<Hive>,
    onNavigateToHiveDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnHiveClick by rememberUpdatedState(onNavigateToHiveDetail)

    val markersRef = remember { mutableStateOf<Map<Long, Marker>>(emptyMap()) }

    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Start at Poland overview — will be overridden once hives load
            controller.setZoom(POLAND_ZOOM)
            controller.setCenter(GeoPoint(POLAND_LAT, POLAND_LON))
        }
    }

    LaunchedEffect(hives) {
        val currentMarkers = markersRef.value.toMutableMap()
        val incomingIds    = hives.map { it.id }.toSet()

        // Remove markers for hives no longer in the list
        (currentMarkers.keys - incomingIds).forEach { id ->
            mapView.overlays.remove(currentMarkers.remove(id))
        }

        // Add or refresh markers
        hives.forEach { hive ->
            val point = GeoPoint(hive.latitude!!, hive.longitude!!)
            val marker = currentMarkers[hive.id] ?: Marker(mapView).also { m ->
                m.infoWindow = null
                mapView.overlays.add(m)
                currentMarkers[hive.id] = m
            }
            marker.position = point
            marker.title    = "Ul #${hive.number} — ${hive.name}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.setOnMarkerClickListener { _, _ ->
                currentOnHiveClick(hive.id)
                true
            }
        }

        markersRef.value = currentMarkers
        mapView.invalidate()

        // Camera: fit all markers or fall back to Poland overview
        mapView.post {
            when {
                hives.isEmpty() -> {
                    mapView.controller.setCenter(GeoPoint(POLAND_LAT, POLAND_LON))
                    mapView.controller.setZoom(POLAND_ZOOM)
                }
                hives.size == 1 -> {
                    mapView.controller.animateTo(
                        GeoPoint(hives[0].latitude!!, hives[0].longitude!!),
                        SINGLE_ZOOM,
                        null
                    )
                }
                else -> {
                    val lats = hives.map { it.latitude!! }
                    val lons = hives.map { it.longitude!! }
                    val box  = BoundingBox(lats.max(), lons.max(), lats.min(), lons.min())
                    mapView.zoomToBoundingBox(box, true, BOUNDS_PAD)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                else                      -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory  = { mapView },
        modifier = modifier.clipToBounds()
    )
}
