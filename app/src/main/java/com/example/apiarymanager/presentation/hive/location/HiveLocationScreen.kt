package com.example.apiarymanager.presentation.hive.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val DEFAULT_LAT = 52.0
private const val DEFAULT_LON = 19.5
private const val POLAND_ZOOM = 7.5
private const val GPS_ZOOM    = 15.0
private const val HIVE_ZOOM   = 15.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveLocationScreen(
    onNavigateBack: () -> Unit,
    viewModel: HiveLocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val mapViewRef      = remember { mutableStateOf<MapView?>(null) }
    val locationOverlay = remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    var showSettingsDialog by remember { mutableStateOf(false) }

    fun flyToGps() {
        val overlay = locationOverlay.value ?: return
        val point   = overlay.myLocation
        if (point != null) {
            mapViewRef.value?.post {
                mapViewRef.value?.controller?.animateTo(point, GPS_ZOOM, 800L)
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Szukanie sygnału GPS…") }
            overlay.runOnFirstFix {
                val fix = overlay.myLocation ?: return@runOnFirstFix
                mapViewRef.value?.post {
                    mapViewRef.value?.controller?.animateTo(fix, GPS_ZOOM, 800L)
                }
            }
        }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationOverlay.value?.enableMyLocation()
            flyToGps()
        } else {
            val activity    = context as? ComponentActivity
            val canAskAgain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)
            } ?: true
            if (!canAskAgain) showSettingsDialog = true
        }
    }

    fun requestGpsOrFly() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) flyToGps() else locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.latitude == null) requestGpsOrFly()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HiveLocationEvent.NavigateBack   -> onNavigateBack()
                is HiveLocationEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Brak dostępu do lokalizacji") },
            text  = {
                Text(
                    "Uprawnienie do lokalizacji zostało trwale odrzucone.\n\n" +
                    "Aby je włączyć:\nUstawienia → Aplikacje → UL Manager → Uprawnienia → Lokalizacja"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }) { Text("Przejdź do ustawień") }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.hiveName.ifEmpty { "Lokalizacja ula" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { requestGpsOrFly() }) {
                        Icon(
                            imageVector        = Icons.Outlined.MyLocation,
                            contentDescription = "Moja lokalizacja",
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text     = "Kliknij na mapie, aby ustawić lokalizację ula",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HiveLocationMapView(
                latitude           = uiState.latitude,
                longitude          = uiState.longitude,
                onMapReady         = { mapView, overlay ->
                    mapViewRef.value      = mapView
                    locationOverlay.value = overlay
                },
                onLocationSelected = viewModel::onLocationSelected,
                modifier           = Modifier.weight(1f).fillMaxWidth()
            )

            Button(
                onClick  = viewModel::onSaveClick,
                enabled  = !uiState.isSaving && uiState.latitude != null,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color       = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.padding(end = 8.dp)
                    )
                }
                Text("Zapisz lokalizację")
            }
        }
    }
}

// ---------------------------------------------------------------------------

@Composable
private fun HiveLocationMapView(
    latitude: Double?,
    longitude: Double?,
    onMapReady: (MapView, MyLocationNewOverlay) -> Unit,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnLocationSelected by rememberUpdatedState(onLocationSelected)
    val currentOnMapReady          by rememberUpdatedState(onMapReady)

    val markerRef = remember { mutableStateOf<Marker?>(null) }

    val mapView = remember(context) {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(POLAND_ZOOM)
            controller.setCenter(GeoPoint(DEFAULT_LAT, DEFAULT_LON))
        }
    }

    val locationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).also {
            it.enableMyLocation()
            mapView.overlays.add(it)
        }
    }

    LaunchedEffect(mapView) {
        currentOnMapReady(mapView, locationOverlay)

        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val point = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                currentOnLocationSelected(point.latitude, point.longitude)
                return true
            }
        })
    }

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            val point  = GeoPoint(latitude, longitude)
            val marker = markerRef.value ?: Marker(mapView).also { m ->
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                m.infoWindow = null
                mapView.overlays.add(m)
                markerRef.value = m
            }
            marker.position = point
            mapView.post { mapView.controller.animateTo(point, HIVE_ZOOM, 800L) }
            mapView.invalidate()
        } else {
            markerRef.value?.let { m ->
                mapView.overlays.remove(m)
                markerRef.value = null
                mapView.invalidate()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); locationOverlay.enableMyLocation() }
                Lifecycle.Event.ON_PAUSE  -> { locationOverlay.disableMyLocation(); mapView.onPause() }
                else                      -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory  = { mapView },
        modifier = modifier.clipToBounds()
    )
}
