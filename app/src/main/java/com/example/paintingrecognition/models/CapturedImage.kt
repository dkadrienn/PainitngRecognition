package com.example.paintingrecognition.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captured_image")
data class CapturedImage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val path: String,
    val creationTimestamp: Long
)
