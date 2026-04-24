package com.example.apiarymanager.presentation.dashboard

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrScannerView(
    onScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Keeps the lambda reference fresh across recompositions without recreating the camera
    val currentOnScanned by rememberUpdatedState(onScanned)
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        val status = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (status == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasPermission) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Wymagane uprawnienie do aparatu", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            // AtomicBoolean lives in the factory closure — survives recompositions,
            // no Compose state overhead, thread-safe for the analyzer thread.
            val alreadyScanned = AtomicBoolean(false)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            if (!alreadyScanned.get()) {
                                decodeQr(imageProxy)?.let { result ->
                                    alreadyScanned.set(true)
                                    currentOnScanned(result)
                                }
                            }
                            imageProxy.close()
                        }
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analyzer
                    )
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Nie udało się uruchomić aparatu", Toast.LENGTH_LONG).show()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier  // size fully controlled by the caller
    )
}

private fun decodeQr(imageProxy: ImageProxy): String? {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
    val source = PlanarYUVLuminanceSource(
        bytes,
        imageProxy.width, imageProxy.height,
        0, 0,
        imageProxy.width, imageProxy.height,
        false
    )
    return try {
        MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: NotFoundException) {
        null
    }
}
