package com.example.paketnikapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import org.json.JSONArray

class ProfileActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val level = sharedPref.getInt("level", -1)
        val id = sharedPref.getString("id", "")
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
    when(level){
        -1 -> {
            var name = remember { mutableStateOf("") }
            var lastName = remember { mutableStateOf("") }
            var email = remember { mutableStateOf("") }
            var userId = id
            LaunchedEffect (Unit) {
                ApiUtil.getClientById(id,
                    onSuccess = {responseBody ->
                        val jsonObject = JSONArray(responseBody.string()).getJSONObject(0)
                        Log.e("ProfileActivityLogger", jsonObject.toString())
                        name.value = jsonObject.getString("firstName")
                        lastName.value = jsonObject.getString("lastName")
                        email.value = jsonObject.getString("email")
                    },
                    onFailure = {error ->
                        Log.e("ProfileActivityLogger", error.toString())
                    })
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "First Name: ${name.value}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Last Name: ${lastName.value}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Email: ${email.value}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "ID: $userId", style = MaterialTheme.typography.bodyMedium)
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