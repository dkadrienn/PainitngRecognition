package com.example.paintingrecognition

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import com.example.paintingrecognition.eventInterfaces.CapturedImageEvent
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.viewModels.ScanViewModel

class ImageSavedCallback(val context: Context, val onEvent: (CapturedImageEvent) -> Unit, val scanViewModel: ScanViewModel): OnImageSavedCallback {
    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        val path = outputFileResults.savedUri.toString()
        val name = path.substring(path.lastIndexOf("/") + 1, path.length)

        onEvent(CapturedImageEvent.SaveCapturedImage(CapturedImage(name = name, path = outputFileResults.savedUri.toString(), creationTimestamp = System.currentTimeMillis())))
        scanViewModel.savePhoto()
        scanViewModel.emmitResult()
    }

    override fun onError(exception: ImageCaptureException) {
        Log.d("ImageSavedCallback", "Error saving photo "+ exception)
    }
}