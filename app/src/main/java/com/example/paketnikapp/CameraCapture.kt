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
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCapture(private val context: Context, private val lifecycleOwner: LifecycleOwner) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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
        val videoFile = File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture?.output
            ?.prepareRecording(context, outputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    if (!event.hasError()) {
                        onVideoSaved(videoFile)
                        // Upload the video file after saving
                        uploadVideo(videoFile)
                    } else {
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
        ApiUtil.sendVideo(videoFile, onSuccess = {
            // Handle successful upload
        }, onFailure = { throwable ->
            // Handle failure
        })
    }
}
