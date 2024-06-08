package com.example.paketnikapp

import android.content.Context
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
                // Handle exception
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startVideoCapture(onVideoSaved: (File) -> Unit) {
        if (isProcessing) return // Prevent multiple captures

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
                        // Handle error
                    }
                }
            }

        // Stop recording after 3 seconds
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
            isProcessing = false // Reset flag on success
        }, onFailure = { throwable ->
            isProcessing = false // Reset flag on failure
            // Handle failure
        })
    }
}
