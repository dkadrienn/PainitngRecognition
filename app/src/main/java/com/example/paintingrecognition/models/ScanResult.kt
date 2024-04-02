package com.example.paintingrecognition.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_result")
data class ScanResult(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    val imageUrl: String,
    val title: String,
    val genre: String,
    val painter: String,
    val resemblance: Float,
    val capturedImageUrl: String
)