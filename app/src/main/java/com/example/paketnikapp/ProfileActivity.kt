package com.example.paketnikapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import org.json.JSONObject

class ProfileActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val level = sharedPreferences.getInt("level", -1)
        val id = sharedPreferences.getString("userId", "")
        Log.e("ProfileActivityLogger", id.toString())
        val idString = id ?: ""
        setContent{
            PaketnikAppTheme {
                Scaffold(
                    bottomBar = { ProfileBottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ProfileAppSurface {
                            displayProfile(level, idString)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun displayProfile(level: Int, id: String) {
    val context = LocalContext.current
    when(level){
        -1 -> {
            var name = remember { mutableStateOf("") }
            var lastName = remember { mutableStateOf("") }
            var email = remember { mutableStateOf("") }
            var userId = remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                try {
                    ApiUtil.getClientById(id,
                        onSuccess = { responseBody ->
                            try {
                                val jsonObject = JSONObject(responseBody.string())
                                Log.e("ProfileActivityLogger", jsonObject.toString())
                                name.value = jsonObject.getString("firstName")
                                lastName.value = jsonObject.getString("lastName")
                                email.value = jsonObject.getString("email")
                                userId.value = id
                            } catch (e: Exception) {
                                Log.e("ProfileActivityLogger", "Error parsing JSON: ${e.toString()}")
                            }
                        },
                        onFailure = { error ->
                            Log.e("ProfileActivityLogger", error.toString())
                        })
                } catch (e: Exception) {
                    Log.e("ProfileActivityLogger", "Error in getClientById: ${e.toString()}")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "First Name: ${name.value}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Last Name: ${lastName.value}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Email: ${email.value}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "ID: ${userId.value}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(context, PackageActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Package")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Room")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Contracts")
                }
            }
        }
        3 -> {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Admin profile")
            }
        }
        else -> {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Staff profile")
            }
        }
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