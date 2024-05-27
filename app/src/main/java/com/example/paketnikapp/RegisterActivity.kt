// src/main/java/com/example/paketnikapp/RegisterActivity.kt
package com.example.paketnikapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaketnikAppTheme {
                RegisterScreen { firstName, lastName, email, password, confirmPassword -> performRegister(firstName, lastName, email, password, confirmPassword) }
            }
        }
    }

    private fun performRegister(firstName: String, lastName: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        ApiUtil.register(firstName, lastName, email, password, onSuccess = { response ->
            Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            if (response.success) {
                // Handle successful registration
            }
        }, onFailure = { throwable ->
            Toast.makeText(this, "Error: ${throwable.message}", Toast.LENGTH_SHORT).show()
        })
    }
}

@Composable
fun RegisterScreen(onRegisterClick: (String, String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onRegisterClick(firstName, lastName, email, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    PaketnikAppTheme {
        RegisterScreen { _, _, _, _, _ -> }
    }
}
