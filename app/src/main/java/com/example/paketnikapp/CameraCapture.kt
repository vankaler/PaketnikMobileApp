package com.example.paketnikapp

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.paketnikapp.apiUtil.ApiUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCapture(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val userId: String
) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing: Boolean = false

    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            videoCapture = VideoCapture.withOutput(Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build())

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, videoCapture
                )
            } catch (exc: Exception) {
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startVideoCapture(onVideoSaved: (File) -> Unit) {
        if (isProcessing) return

        isProcessing = true
        val videoFile = File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture?.output
            ?.prepareRecording(context, outputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    if (!event.hasError()) {
                        onVideoSaved(videoFile)
                        uploadVideo(videoFile)
                    } else {
                        isProcessing = false
                    }
                }
            }

        Handler(Looper.getMainLooper()).postDelayed({
            stopVideoCapture()
        }, 3000)
    }

    fun stopVideoCapture() {
        recording?.stop()
        recording = null
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }

    private fun uploadVideo(videoFile: File) {
        val clientIdPart = userId.toRequestBody("text/plain".toMediaTypeOrNull())
        ApiUtil.uploadVideo(videoFile, clientIdPart, onSuccess = {
            isProcessing = false
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }, onFailure = { throwable ->
            isProcessing = false
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        })
    }
}
