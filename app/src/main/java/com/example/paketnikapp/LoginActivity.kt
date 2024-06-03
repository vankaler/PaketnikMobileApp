package com.example.paketnikapp

import android.Manifest
import okhttp3.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import java.io.IOException

class LoginActivity : ComponentActivity() {

    lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Zahteva za dovoljenje za pošiljanje obvestil
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        setContent {
            PaketnikAppTheme {
                LoginScreen(
                    onFaceIdClick = { startCameraActivity() },
                    onLoginClick = { email, password -> performLogin(email, password) },
                    onRegisterClick = {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun startCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun capturePhoto() {
        val photoFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@LoginActivity, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@LoginActivity, "Photo capture succeeded: ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                    // Handle the captured photo as needed
                }
            }
        )
    }

    private fun performLogin(email: String, password: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("LoginActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Pošljite token vašemu strežniku
            sendTokenToServer(token, email, password)
        }
    }

    private fun sendTokenToServer(token: String, email: String, password: String) {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", null)

        val client = OkHttpClient()
        val json = """
        {
            "userId": "$userId",
            "fcmToken": "$token"
        }
    """
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)
        val request = Request.Builder()
            .url("http://10.0.2.2:3001/clients/register-fcm-token")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginActivity", "Failed to send FCM token: ${e.message}")
                // Handle the failure to register token
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("LoginActivity", "FCM token sent successfully")
                    // After successfully registering the token, proceed with login
                    attemptLogin(email, password)
                } else {
                    Log.e("LoginActivity", "Failed to send FCM token: ${response.message}")
                }
            }
        })
    }

    private fun attemptLogin(email: String, password: String) {
        ApiUtil.login(email, password, onSuccess = { response ->
            val message = response.message ?: "Login successful"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            if (response.success) {
                // Save login state
                val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()

                // Log for debugging
                Log.d("LoginActivity", "Login successful, launching MainActivity")

                // Launch MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, onFailure = { throwable ->
            val message = throwable.message ?: "An error occurred"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

            // Log for debugging
            Log.d("LoginActivity", "Login failed: ${throwable.message}")
        })
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

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun LoginScreen(onFaceIdClick: () -> Unit, onLoginClick: (String, String) -> Unit, onRegisterClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        val imageCapture = ImageCapture.Builder().build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            (context as LoginActivity).imageCapture = imageCapture
        } catch (exc: Exception) {
            Toast.makeText(context, "Use case binding failed", Toast.LENGTH_SHORT).show()
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

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

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
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = rememberImagePainter(data = R.drawable.face_scan),
            contentDescription = "Face ID Login",
            modifier = Modifier
                .size(64.dp)
                .clickable { onFaceIdClick() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Don't have an account? Register",
            fontSize = 16.sp,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.clickable { onRegisterClick() }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    PaketnikAppTheme {
        LoginScreen(onFaceIdClick = {}, onLoginClick = { _, _ -> }, onRegisterClick = {})
    }
}
