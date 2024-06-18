package com.example.paintingrecognition.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.paintingrecognition.views.main.fragments.HistoryFragment
import com.example.paintingrecognition.views.main.fragments.HomeFragment
import com.example.paintingrecognition.R
import com.example.paintingrecognition.views.main.fragments.ScanFragment
import com.example.paintingrecognition.views.main.fragments.ScanResultFragment
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel

class Navigator(private val supportFragmentManager: FragmentManager, private val capturedImageViewModel: CapturedImageViewModel, private val scanViewModel: ScanViewModel) {

    fun openHomeFragment() {
        replaceFragment(HomeFragment(capturedImageViewModel, scanViewModel))
    }

    fun openHistoryFragment() {
        replaceFragment(HistoryFragment(capturedImageViewModel, scanViewModel))
    }

    fun openScanFragment() {
        replaceFragment(ScanFragment(capturedImageViewModel, scanViewModel))
    }

    fun openScanResultFragment(capturedImage: CapturedImage) {
        replaceFragment(ScanResultFragment(scanViewModel, capturedImageViewModel, capturedImage))
    }

    /*fun openImageResultDetailPage(capturedImage: CapturedImage, scanResult: ScanResult?) {
        replaceFragment(ImageResultDetailPage(capturedImage, scanResult, scanViewModel))
    }*/

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}