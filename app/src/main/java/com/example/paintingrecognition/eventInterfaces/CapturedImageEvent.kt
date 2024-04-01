package com.example.paintingrecognition.eventInterfaces

import com.example.paintingrecognition.models.CapturedImage

interface CapturedImageEvent {
    data class SaveCapturedImage(val capturedImage: CapturedImage): CapturedImageEvent
    data class DeleteCapturedImage(val capturedImage: CapturedImage): CapturedImageEvent
    object GetLastCapturedImages: CapturedImageEvent
}