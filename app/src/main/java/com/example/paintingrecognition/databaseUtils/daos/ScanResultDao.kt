package com.example.paintingrecognition.databases.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.paintingrecognition.models.ScanResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Insert
    suspend fun upsertScanResult(scanResult: ScanResult)

    @Delete
    suspend fun deleteScanResult(scanResult: ScanResult)

    @Query("DELETE FROM scan_result WHERE capturedImageUrl in (:url)")
    suspend fun deleteScanResultByUrl(url: String)

    /**
     * Returning all scan results.
     */
    @Query("SELECT * FROM scan_result")
    fun getScanResults(): Flow<List<ScanResult>>

    @Query("SELECT * FROM scan_result WHERE capturedImageUrl in (:url) LIMIT 1")
    fun getScanResultByImageUrl(url: String): Flow<ScanResult>

}