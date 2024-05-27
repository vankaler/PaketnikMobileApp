package com.example.paketnikapp

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.qr.QrScanActivity
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import androidx.compose.animation.core.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.util.Base64

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContent {
            PaketnikAppTheme {
                AppSurface { startQRScanner() }
            }
        }
    }

    private fun startQRScanner() {
        val intent = Intent(this, QrScanActivity::class.java)
        startActivityForResult(intent, QR_SCANNER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            val qrContent = data?.getStringExtra("QR_CONTENT")
            qrContent?.let {
                openBox(it, this)
            }
        }
    }

    private fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val QR_SCANNER_REQUEST_CODE = 1
    }
}

private fun openBox(boxId: String, context: Context) {
    val client = OkHttpClient()
    val json = """
        {
            "boxId": $boxId,
            "tokenFormat": 0
        }
    """
    val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)
    val request = Request.Builder()
        .url("https://api-d4me-stage.direct4.me/sandbox/v1/Access/openbox")
        .post(body)
        .addHeader("x-api-key", "9ea96945-3a37-4638-a5d4-22e89fbc998f")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                showMessage(context, "Failed to open box: ${e.message}")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.let { responseBody ->
                val responseData = JSONObject(responseBody.string())
                val base64Token = responseData.getString("data")
                playToken(base64Token, context)
            }
        }
    })
}

private fun playToken(base64Token: String, context: Context) {
    try {
        // Decode Base64 token and save as .wav file
        val decodedBytes = Base64.decode(base64Token, Base64.DEFAULT)
        val tokenFile = File(context.cacheDir, "token.wav")
        FileOutputStream(tokenFile).use { it.write(decodedBytes) }

        // Play the .wav file
        val mediaPlayer = MediaPlayer().apply {
            setDataSource(tokenFile.absolutePath)
            prepare()
            setOnCompletionListener {
                it.release()
                Handler(Looper.getMainLooper()).post {
                    showMessage(context, "Box opened successfully!")
                }
            }
            start()
        }
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post {
            showMessage(context, "Failed to play token: ${e.message}")
        }
    }
}

private fun showMessage(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

@Composable
fun AppSurface(onScanClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainPage(onScanClick)
    }
}

@Composable
fun MainPage(onScanClick: () -> Unit) {
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 0.75f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.75f at 0 with FastOutSlowInEasing
                1f at 500 with FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Greeting("User")
        InstructionText()
        Spacer(modifier = Modifier.height(32.dp))
        ScanButton(pulseScale, onScanClick)
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = "Welcome, $name!",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
fun InstructionText() {
    Text(
        "Scan the QR code to unlock your mailbox.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ScanButton(pulseScale: Float, onScanClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onScanClick() },
        modifier = Modifier.scale(pulseScale),
        shape = CircleShape
    ) {
        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Scan QR Code")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PaketnikAppTheme {
        AppSurface {}
    }
}
