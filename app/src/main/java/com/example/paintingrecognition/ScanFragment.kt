package com.example.paintingrecognition

import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paintingrecognition.adapters.ScanResultsAdapter
import com.example.paintingrecognition.databinding.FragmentScanBinding
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.viewModels.ScanViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScanFragment : Fragment() {


    private lateinit var binding: FragmentScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var scanViewModel: ScanViewModel

    private lateinit var scanResultsAdapter: ScanResultsAdapter

    private var scanDisposable: Disposable? = null

    companion object {
        private val TAG: String = ScanFragment::class.java.simpleName
        private val CAMERAX_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()
        scanViewModel = ScanViewModel()

        registerForScanChanges()
        setUpScanResultRecyclerView()

        // Ask for camera permission
        if (!hasRequiredPermissions()) {
            checkCameraPermission()
        } else {
            startCamera()
        }

        return binding.root
    }

    /**
     * Requires camera permission.
     */
    private fun checkCameraPermission() {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                startCamera()
            }
        }.launch(android.Manifest.permission.CAMERA)
    }

    /**
     * Checks if permissions are granted.
     */
    private fun hasRequiredPermissions(): Boolean {
        context?.let {
            return CAMERAX_PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(it, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    /**
     * Starts the camera
     */
    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(requireContext())
        processCameraProvider.addListener({
            try {
                val cameraProvider = processCameraProvider.get()
                startCameraX(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting the camera")
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startCameraX(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll();

        // camera selector use case
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // preview use case
        val preview: Preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        }

        // image capture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }

    fun capturePhoto() {
        val timestamp: Long = System.currentTimeMillis()

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(
                requireActivity().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build(),
            cameraExecutor,
            ImageSavedCallback(requireContext(), scanViewModel)
        )
    }

    /**
     * Observes to scan results.
     */
    private fun registerForScanChanges() {
        scanViewModel.scanResultSubject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: Observer<MutableList<ScanResult>> {
            override fun onSubscribe(d: Disposable) {
                scanDisposable = d
            }

            override fun onError(e: Throwable) {
            }

            override fun onComplete() {
            }

            override fun onNext(scanResults: MutableList<ScanResult>) {
                Log.d(TAG, "Scan results arrived with size: " + scanResults.size)
                scanResultsAdapter.scanResults = scanResults
                scanResultsAdapter.notifyDataSetChanged()
            }

        })
    }

    private fun setUpScanResultRecyclerView() {
        scanResultsAdapter = ScanResultsAdapter(mutableListOf(), context)
        binding.scanRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.scanRecyclerView.adapter = scanResultsAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanDisposable?.let {
            if (!it.isDisposed) {
                it.dispose()
                scanDisposable = null
            }
        }
    }

}