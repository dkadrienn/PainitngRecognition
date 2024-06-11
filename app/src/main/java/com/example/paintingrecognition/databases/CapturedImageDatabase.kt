package com.example.paintingrecognition.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.paintingrecognition.daos.CapturedImageDao
import com.example.paintingrecognition.daos.ScanResultDao
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.ScanResult

@Database(
    entities = [CapturedImage::class, ScanResult::class],
    version = 2
)
abstract class CapturedImageDatabase: RoomDatabase() {

    abstract val capturedImageDao: CapturedImageDao
    abstract val scanResultDao: ScanResultDao
}