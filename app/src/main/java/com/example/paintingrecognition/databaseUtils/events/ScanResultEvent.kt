package com.example.paintingrecognition.databases.events

import com.example.paintingrecognition.models.ScanResult

interface ScanResultEvent {
    data class SaveScanResult(val scanResult: ScanResult): ScanResultEvent
    data class DeleteScanResult(val scanResult: ScanResult): ScanResultEvent
    data class DeleteScanResultByUrl(val url: String): ScanResultEvent
    data class GetScanResultByUrl(val url: String): ScanResultEvent
    object GetScanResults: ScanResultEvent
}