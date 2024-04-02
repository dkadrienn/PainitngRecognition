package com.example.paintingrecognition

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import com.example.paintingrecognition.models.CapturedImage

class ImageSavedCallback(val context: Context): OnImageSavedCallback {
    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        val path = outputFileResults.savedUri.toString()
        val name = path.substring(path.lastIndexOf("/") + 1, path.length)

        val capturedImage = CapturedImage(name = name, path = outputFileResults.savedUri.toString(), creationTimestamp = System.currentTimeMillis())
        MainActivity.navigation.openScanResultFragment(capturedImage)
    }

    override fun onError(exception: ImageCaptureException) {
        Log.d("ImageSavedCallback", "Error saving photo "+ exception)
    }
}