package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.paketnikapp.ui.theme.PaketnikAppTheme

class CameraActivity : ComponentActivity() {
    private lateinit var cameraCapture: CameraCapture
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = intent.getStringExtra("userId") ?: run {
            Toast.makeText(this, "User ID not available", Toast.LENGTH_SHORT).show()
            Log.e("CameraActivity", "User ID not found in Intent")
            finish()
            return
        }

        cameraCapture = CameraCapture(this, this, userId)

        setContent {
            PaketnikAppTheme {
                val context = LocalContext.current
                val previewView = remember { PreviewView(context) }

                cameraCapture.startCamera(previewView)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { startVideoCapture() }, modifier = Modifier.padding(8.dp)) {
                        Text("Start Capture")
                    }
                }
            }
        }
    }

    private fun startVideoCapture() {
        cameraCapture.startVideoCapture { videoFile ->
            Toast.makeText(this, "Video saved: ${videoFile.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraCapture.shutdown()
    }
}
