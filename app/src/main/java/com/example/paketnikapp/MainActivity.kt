package com.example.paketnikapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import com.example.paketnikapp.ui.theme.PaketnikAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaketnikAppTheme {
                AppSurface()
            }
        }
    }
}

@Composable
fun AppSurface() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MainPage()
    }
}

@Composable
fun MainPage() {
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
        ScanButton(pulseScale)
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
fun ScanButton(pulseScale: Float) {
    FloatingActionButton(
        onClick = { /* TODO: Implement QR Code Scanning */ },
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
        AppSurface()
    }
}
