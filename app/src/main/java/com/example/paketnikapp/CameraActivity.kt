package com.example.paketnikapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import com.example.paketnikapp.util.CameraUtil

class CameraActivity : ComponentActivity() {
    private lateinit var cameraUtil: CameraUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraUtil = CameraUtil(this, this)

        setContent {
            PaketnikAppTheme {
                CameraScreen(
                    onCaptureClick = { startVideoCapture() },
                    onStopClick = { stopVideoCapture() }
                )
            }
        }
    }

    private fun startVideoCapture() {
        cameraUtil.startVideoCapture { videoFile ->
            Toast.makeText(this, "Video saved: ${videoFile.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopVideoCapture() {
        cameraUtil.stopVideoCapture()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraUtil.shutdown()
    }
}

@Composable
fun CameraScreen(onCaptureClick: () -> Unit, onStopClick: () -> Unit) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraUtil = remember { CameraUtil(context, context as CameraActivity) }

    cameraUtil.startCamera(previewView)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onCaptureClick, modifier = Modifier.padding(8.dp)) {
                Text("Start Capture")
            }
            Button(onClick = onStopClick, modifier = Modifier.padding(8.dp)) {
                Text("Stop Capture")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    PaketnikAppTheme {
        CameraScreen(onCaptureClick = {}, onStopClick = {})
    }
}
