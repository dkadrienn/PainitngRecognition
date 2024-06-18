package com.example.paintingrecognition.databaseUtils

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.paintingrecognition.databases.daos.CapturedImageDao
import com.example.paintingrecognition.databases.daos.ScanResultDao
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