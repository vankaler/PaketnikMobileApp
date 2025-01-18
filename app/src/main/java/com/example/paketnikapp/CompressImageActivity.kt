// CompressImageActivity.kt

package com.example.paketnikapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.example.paketnikapp.util.CompressionHelper
import java.io.File
import java.io.FileOutputStream

class CompressImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializacija Chaquopy Python
        if (!Python.isStarted()) {
            Python.start(com.chaquo.python.android.AndroidPlatform(this))
        }
        setContent {
            CompressImageScreen()
        }
    }
}

@Composable
fun CompressImageScreen() {
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var compressedFilePath by remember { mutableStateOf<String?>(null) }

    // Launcher za izbiro slike iz galerije
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Launcher za zahtevanje dovoljenj
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(context, "Dovoljenja so potrebna za delovanje aplikacije", Toast.LENGTH_SHORT).show()
        }
    }

    // Zahtevajte dovoljenja ob zagonu
    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gumb za izbiro slike
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Izberi Sliko")
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            // Prikaz izbrane slike
            val bitmap = remember(uri) {
                uriToBitmap(context, uri)
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gumb za kompresijo in shranjevanje
            Button(onClick = {
                // Shranjevanje izbrane slike v notranjo shrambo
                val inputPath = copyUriToInternalStorage(context, uri, "input_image")
                if (inputPath == null) {
                    Toast.makeText(context, "Neuspešno kopiranje slike", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Pot za shranjevanje komprimirane datoteke
                val outputPath = "${context.filesDir}/compressed_image.bin"

                // Klic funkcije za kompresijo
                val success = CompressionHelper.compressImage(context, inputPath, outputPath)

                if (success) {
                    compressedFilePath = outputPath
                    Toast.makeText(context, "Kompresija uspešna!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Kompresija ni uspela.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Kompresiraj in Shrani")
            }
        }

        // Prikaz poti do komprimirane datoteke
        compressedFilePath?.let { path ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kompresirana datoteka shranjena na: $path")
        }
    }
}

// Funkcija za pretvorbo URI v Bitmap
fun uriToBitmap(context: Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Funkcija za kopiranje URI datoteke v interno shrambo
fun copyUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val extension = getFileExtension(context, uri)
        val outputFileName = if (extension != null) "$fileName.$extension" else fileName
        val outputFile = File(context.filesDir, outputFileName)
        val outputStream = FileOutputStream(outputFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        outputFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Funkcija za pridobitev razširitve datoteke iz URI
fun getFileExtension(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)?.substringAfter('/')
}
