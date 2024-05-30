package com.example.paketnikapp.util

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recording
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraUtil(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var currentRecording: Recording? = null
    private var preview: Preview? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            videoCapture = VideoCapture.withOutput(Recorder.Builder().build())
            preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    videoCapture,
                    preview
                )
            } catch (exc: Exception) {
                Log.e("CameraUtil", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun startVideoCapture(onVideoCaptured: (File) -> Unit) {
        val videoCapture = videoCapture ?: return

        val videoFile = File(context.externalMediaDirs.firstOrNull(), "captured_video.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        currentRecording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> Log.d("CameraUtil", "Recording started")
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            onVideoCaptured(videoFile)
                        } else {
                            Log.e("CameraUtil", "Recording error: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    fun stopVideoCapture() {
        currentRecording?.stop()
        currentRecording = null
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
