package com.example.paintingrecognition.viewModels

import android.os.Handler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintingrecognition.daos.ScanResultDao
import com.example.paintingrecognition.eventInterfaces.ScanResultEvent
import com.example.paintingrecognition.models.ScanResult
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val dao: ScanResultDao
): ViewModel() {

    val _scanResults = dao.getScanResults()
    lateinit var _scanResultByUrl: Flow<ScanResult>

    var scanResultSubject = PublishSubject.create<MutableList<ScanResult>>()

    fun onEvent(event: ScanResultEvent) {
        when (event) {
            is ScanResultEvent.SaveScanResult -> {
                viewModelScope.launch(Dispatchers.IO) {
                    dao.upsertScanResult(event.scanResult)
                }
            }
            is ScanResultEvent.DeleteScanResult -> {
                viewModelScope.launch {
                    dao.deleteScanResult(event.scanResult)
                }
            }
            is ScanResultEvent.DeleteScanResultByUrl -> {
                viewModelScope.launch {
                    dao.deleteScanResultByUrl(event.url)
                }
            }
            is ScanResultEvent.GetScanResultByUrl -> {
                _scanResultByUrl = dao.getScanResultByImageUrl(event.url)
            }
            ScanResultEvent.GetScanResults -> {
                _scanResults
            }

        }
    }

    /**
     * Emitting the result fom db.
     */
    fun emmitResult(items: MutableList<ScanResult>) {

        items.sortByDescending { it.resemblance }
        Handler().postDelayed(Runnable {
            scanResultSubject.onNext(items)
        },1000)
    }
}