package com.example.paketnikapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.qr.QrScanActivity
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        setContent {
            PaketnikAppTheme {
                Scaffold(
                    bottomBar = { BottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        AppSurface {
                            startQRScanner()
                        }
                    }
                }
            }

        }
    }

    private fun requestNotificationPermission() {
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun startQRScanner() {
        val intent = Intent(this, QrScanActivity::class.java)
        startActivityForResult(intent, QR_SCANNER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            val qrContent = data?.getStringExtra("QR_CONTENT")
            Log.e("QR Content", qrContent ?: "null")
            if (qrContent != null) {
                openBox(qrContent, this)
            } else {
                showMessage(this, "Failed to get box ID from QR code")
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
    Log.e("Request", "Opening box with ID: $boxId")
    val client = OkHttpClient()
    val path = boxId.substringAfter("HTTPS://B.DIRECT4.ME/")
    val pathWithoutTrailingSlash = path.removeSuffix("/")
    Log.e("Request", "Opening box with ID: $pathWithoutTrailingSlash")
    val parts = pathWithoutTrailingSlash.split("/")
    val boxIdWithLeadingZeros = parts[1]
    val splitBoxId = boxIdWithLeadingZeros.trimStart('0')
    val splitBoxIdInt = splitBoxId.toInt()
    val qrCodeInfo = pathWithoutTrailingSlash
    Log.e("Request", "Split box ID: $splitBoxId")
    val json = """
    {
        "deliveryId": 0,
        "boxId": $splitBoxIdInt,
        "tokenFormat": 5,
        "latitude": null,
        "longitude": null,
        "qrCodeInfo": "$qrCodeInfo",
        "terminalSeed": null,
        "isMultibox": false,
        "doorIndex": null,
        "addAccessLog": false
    }
    """
    Log.e("Request", json)
    val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)
    val request = Request.Builder()
        .url("https://api-d4me-stage.direct4.me/sandbox/v1/Access/openbox")
        .post(body)
        .addHeader("Authorization", "Bearer 9ea96945-3a37-4638-a5d4-22e89fbc998f")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post {
                showMessage(context, "Failed to open box: ${e.message}")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                Log.e("Response", "Request failed with status code: ${response.code}")
                if (response.code == 400 && response.body != null) {
                    Log.e("Response", "Response body: ${response.body!!.string()}")
                }
                Handler(Looper.getMainLooper()).post {
                    showMessage(context, "Request failed with status code: ${response.code}")
                }
            } else {
                response.body?.let { responseBody ->
                    val responseBodyString = responseBody.string()
                    if (responseBodyString.isNotBlank()) {
                        Log.e("Response", responseBodyString)
                        val responseData = JSONObject(responseBodyString)
                        val base64Token = responseData.getString("data")
                        playToken(base64Token, context)
                    } else {
                        Log.e("Response", "Response body is empty")
                        Handler(Looper.getMainLooper()).post {
                            showMessage(context, "Response body is empty")
                        }
                    }
                }
            }
        }
    })
}

private fun playToken(base64Token: String, context: Context) {
    Log.e("Token", base64Token.substring(0, 10) + "...")

    try {
        // Decode Base64 token and save as .wav file
        val decodedBytes = Base64.decode(base64Token, Base64.DEFAULT)
        val tokenFile = File(context.cacheDir, "token.wav")
        FileOutputStream(tokenFile).use { it.write(decodedBytes) }

        Log.e("Token", "Decoded token saved to: ${tokenFile.absolutePath}")

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
            Log.e("Token", "Failed to play token: ${e.message}")
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
        Spacer(modifier = Modifier.height(32.dp))
        LogoutButton()
    }
}

@Composable
fun BottomBar() {
    val context = LocalContext.current
    BottomAppBar {
        Button(onClick = {
            val intent = Intent(context, InformationActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Information")
        }

        Button(onClick = {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }) {
            Text("Login")
        }

        // Add more buttons for more activities
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

@Composable
fun LogoutButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Logout")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PaketnikAppTheme {
        AppSurface {}
    }
}
