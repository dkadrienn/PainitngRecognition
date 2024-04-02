package com.example.paintingrecognition

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel

class Navigation(private val supportFragmentManager: FragmentManager, private val capturedImageViewModel: CapturedImageViewModel, private val scanViewModel: ScanViewModel) {

    fun openHomeFragment() {
        replaceFragment(HomeFragment(capturedImageViewModel, scanViewModel))
    }

    fun openHistoryFragment() {
        replaceFragment(HistoryFragment(capturedImageViewModel))
    }

    fun openScanFragment() {
        replaceFragment(ScanFragment(capturedImageViewModel, scanViewModel))
    }

    fun openScanResultFragment(capturedImage: CapturedImage) {
        replaceFragment(ScanResultFragment(scanViewModel, capturedImageViewModel, capturedImage))
    }

    fun openImageResultDetailPage(capturedImage: CapturedImage, scanResult: ScanResult?) {
        replaceFragment(ImageResultDetailPage(capturedImage, scanResult, scanViewModel))
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction();
        transaction.replace(R.id.mainContainter, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}