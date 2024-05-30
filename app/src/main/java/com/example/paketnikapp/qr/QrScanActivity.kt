package com.example.paketnikapp.qr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.zxing.integration.android.IntentIntegrator

class QrScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initiateScan()
    }

    private fun initiateScan() {
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Scan a QR code")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                // User cancelled the scan
                showMessage("Cancelled")
            } else {
                // QR code content
                val qrContent = result.contents
                showMessage("Scanned: $qrContent")
                // Pass the result back to the calling activity
                val resultIntent = Intent()
                resultIntent.putExtra("QR_CONTENT", qrContent)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
