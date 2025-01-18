package com.example.paketnikapp.util

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import java.io.File
import java.io.FileOutputStream

object CompressionHelper {

    fun compressImage(context: Context, inputPath: String, outputPath: String): Boolean {
        return try {
            val py = Python.getInstance()
            val compressionModule = py.getModule("compression")

            // Klic funkcije compress_image v Pythonu
            val compressedData = compressionModule.callAttr("compress_image", inputPath)

            // Pretvorba Python bytes v Kotlin ByteArray
            val byteArray = compressedData.toJava(ByteArray::class.java)

            // Shranjevanje kompresiranih podatkov v izhodno datoteko
            val outputFile = File(outputPath)
            FileOutputStream(outputFile).use { it.write(byteArray) }

            true
        } catch (e: Exception) {
            Log.e("CompressionHelper", "Error during compression: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
