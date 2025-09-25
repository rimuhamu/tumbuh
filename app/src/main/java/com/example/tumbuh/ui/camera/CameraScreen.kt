// File: app/src/main/java/com/example/tumbuh/ui/camera/CameraScreen.kt

package com.example.tumbuh.ui.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera provider and preview
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageCaptured(it) }
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()
    }

    if (cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Camera Preview
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_START

                        // Setup camera
                        cameraProvider?.let { provider ->
                            try {
                                provider.unbindAll()

                                preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(surfaceProvider)
                                }

                                imageCapture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()

                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (exc: Exception) {
                                onError("Camera setup failed: ${exc.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Camera Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Button
                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Open Gallery")
                }

                // Capture Button
                FloatingActionButton(
                    onClick = {
                        capturePhoto(
                            imageCapture = imageCapture,
                            context = context,
                            onImageCaptured = onImageCaptured,
                            onError = onError
                        )
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Placeholder for additional controls
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    } else {
        // Permission not granted
        PermissionRequestScreen(
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
        )
    }
}

private fun capturePhoto(
    imageCapture: ImageCapture?,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture ?: return

    val photoFile = File(
        context.filesDir,
        "plant_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())}.jpg"
    )

    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                onError("Photo capture failed: ${exception.message}")
            }
        }
    )
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app needs camera permission to identify plants. Please grant permission to continue.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Grant Permission")
        }
    }
}
