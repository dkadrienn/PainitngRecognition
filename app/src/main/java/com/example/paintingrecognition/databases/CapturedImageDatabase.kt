package com.example.paintingrecognition.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.paintingrecognition.daos.CapturedImageDao
import com.example.paintingrecognition.models.CapturedImage

@Database(
    entities = [CapturedImage::class],
    version = 1
)
abstract class CapturedImageDatabase: RoomDatabase() {

    abstract val dao: CapturedImageDao
}