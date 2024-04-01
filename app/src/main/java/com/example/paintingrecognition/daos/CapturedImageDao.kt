package com.example.paintingrecognition.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.paintingrecognition.models.CapturedImage
import kotlinx.coroutines.flow.Flow

@Dao
interface CapturedImageDao {

    @Upsert
    suspend fun upsertCapturedImage(capturedImage: CapturedImage)

    @Delete
    suspend fun deleteCapturedImage(capturedImage: CapturedImage)

    /**
     * Returning all images captured on the Scan Fragment.
     */
    @Query("SELECT * FROM captured_image ORDER BY creationTimestamp DESC")
    fun getCapturedImagesByLastCreated(): Flow<List<CapturedImage>>

    @Query("SELECT * FROM captured_image WHERE name in (:name) LIMIT 1")
    fun getCapturedImageByName(name: String): CapturedImage
}