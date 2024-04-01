package com.example.paintingrecognition

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.core.ImageCaptureException
import com.example.paintingrecognition.viewModels.ScanViewModel

class ImageSavedCallback(val context: Context, val scanViewModel: ScanViewModel): OnImageSavedCallback {
    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        Log.d("IMAGESAVE", "Photo saved successfully " + outputFileResults.savedUri)
        scanViewModel.savePhoto()
        scanViewModel.emmitResult()
    }

    override fun onError(exception: ImageCaptureException) {
        Log.d("IMAGESAVE", "Error saving photo "+ exception)
    }
}