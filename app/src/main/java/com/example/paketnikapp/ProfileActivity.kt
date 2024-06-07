package com.example.paketnikapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.paketnikapp.ui.theme.PaketnikAppTheme

class ProfileActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            PaketnikAppTheme {
                Scaffold(
                    bottomBar = { ProfileBottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ProfileAppSurface {
                            displayProfile()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun displayProfile() {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "Scan QR code")
    }
}

@Composable
fun ProfileAppSurface(onScanClick: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        onScanClick()
    }
}

@Composable
fun ProfileBottomBar() {
    val context = LocalContext.current
    BottomAppBar {
        Button(onClick = {
            val intent = Intent(context, InformationActivity::class.java)
            context.startActivity(intent)
        }) {
            androidx.compose.material3.Text("Information")
        }

        Button(onClick = {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }) {
            androidx.compose.material3.Text("Login")
        }

        Button(onClick = {
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        }, enabled = false) {
            androidx.compose.material3.Text("Settings")
        }
    }
}