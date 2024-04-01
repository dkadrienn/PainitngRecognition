package com.example.paintingrecognition.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintingrecognition.daos.CapturedImageDao
import com.example.paintingrecognition.eventInterfaces.CapturedImageEvent
import com.example.paintingrecognition.models.CapturedImage
import kotlinx.coroutines.launch

class CapturedImageViewModel(
    private val dao: CapturedImageDao
): ViewModel() {

    val _capturedImages = dao.getCapturedImagesByLastCreated()
    var loadedCapturedImages: List<CapturedImage> = arrayListOf()

    fun onEvent(event: CapturedImageEvent) {
        when (event) {
            is CapturedImageEvent.SaveCapturedImage -> {
                viewModelScope.launch {
                    dao.upsertCapturedImage(event.capturedImage)
                }
            }
            is CapturedImageEvent.DeleteCapturedImage -> {
                viewModelScope.launch {
                    dao.deleteCapturedImage(event.capturedImage)
                }
            }
            CapturedImageEvent.GetLastCapturedImages -> {
                _capturedImages
            }

        }
    }
}