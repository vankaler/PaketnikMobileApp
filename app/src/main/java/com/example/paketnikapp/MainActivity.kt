package com.example.paketnikapp

import android.content.Intent
import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            showMessage("Scanned: $qrContent")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val QR_SCANNER_REQUEST_CODE = 1
    }
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
