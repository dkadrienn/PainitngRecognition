package com.example.paintingrecognition.models

data class ScanResult(
    val imageUrl: String,
    val title: String,
    val genre: String,
    val painter: String,
    val resemblance: Float
)