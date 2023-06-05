package com.banana.cameracompose

import android.net.Uri
import android.os.Environment
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.banana.cameracompose.ui.theme.iconBig
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
) {
    CameraView()
}

@Composable
fun CameraView() {
    val executor = Executors.newSingleThreadExecutor()
    var images by remember {
       mutableStateOf(listOf<Uri>())
    }
    var lensFacing by remember {
        mutableStateOf(CameraSelector.LENS_FACING_BACK)
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = androidx.camera.core.Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    var camera: Camera? by remember {
        mutableStateOf(null)
    }
    var torchState by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = torchState) {
        if (camera?.cameraInfo?.hasFlashUnit() == true)
            camera?.cameraControl?.enableTorch(torchState)
    }

    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()


    LaunchedEffect(key1 = lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
    }
    CameraControls(lensFacing = lensFacing,
        torchState = torchState,
        images = images,
        onImageCaptureClicked = {
            takePhoto(
                filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                imageCapture = imageCapture,
                outputDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                executor = executor,
                onImageCaptured = {
                    images = images.toMutableList().apply {
                        add(it)
                    }
                }, onError = { it.printStackTrace() })
        },
        onChangeCameraClicked = {
            torchState = false
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else CameraSelector.LENS_FACING_BACK
        }, onTorchClicked = {
            torchState = !torchState
        })
}

@Preview
@Composable
fun CameraControls(
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    torchState: Boolean = false,
    images: List<Uri>? = null,
    onImageCaptureClicked: () -> Unit = {},
    onChangeCameraClicked: () -> Unit = {},
    onTorchClicked: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (lensFacing == CameraSelector.LENS_FACING_BACK) IconButton(
            onClick = onTorchClicked, modifier = Modifier
                .size(
                    iconBig
                )
        ) {
            Icon(
                painterResource(
                    id = if (torchState) R.drawable.ic_flash_on
                    else R.drawable.ic_flash_off
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                tint = Color.White,
                contentDescription = null
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            IconButton(
                onClick = onGalleryClick, modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        Color.Gray
                    )
            ) {
//                if (!images.isNullOrEmpty()) AsyncImage(
//                    model = rememberAsyncImagePainter(data = images.first()),
//                    contentDescription = null
//                )

            }
            IconButton(
                onClick = onImageCaptureClicked, modifier = Modifier
                    .size(
                        iconBig
                    )
            ) {
                Icon(
                    painterResource(
                        R.drawable.ic_capture
                    ),
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White,
                    contentDescription = null
                )
            }
            IconButton(
                onClick = onChangeCameraClicked, modifier = Modifier
                    .size(
                        iconBig
                    )
            ) {
                Icon(
                    painterResource(
                        R.drawable.ic_flip_camera
                    ),
                    modifier = Modifier.fillMaxSize(),
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }

    }
}

@Preview
@Composable
fun CameraScreenPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        CameraScreen()
    }
}