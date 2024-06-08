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
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import java.io.File

class CameraActivity : ComponentActivity() {
    private lateinit var cameraCapture: CameraCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraCapture = CameraCapture(this, this)

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
            uploadVideo(videoFile)
        }
    }

    private fun uploadVideo(videoFile: File) {
        ApiUtil.sendVideo(videoFile, onSuccess = {
            runOnUiThread {
                Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }, onFailure = { throwable ->
            runOnUiThread {
                Toast.makeText(this, "Failed to upload video: ${throwable.message}", Toast.LENGTH_SHORT).show()
            }
            Log.e("CameraActivity", "Error uploading video", throwable)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraCapture.shutdown()
    }
}
