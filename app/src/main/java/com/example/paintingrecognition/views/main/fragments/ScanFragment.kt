package com.example.paintingrecognition.views.main.fragments

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databinding.FragmentScanBinding
import com.example.paintingrecognition.utils.ImageSavedCallback
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScanFragment(private val capturedImageViewModel: CapturedImageViewModel, private val scanViewModel: ScanViewModel) : Fragment(), SensorEventListener {


    private lateinit var binding: FragmentScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    // for rotation checks
    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private lateinit var dialog: AlertDialog

    companion object {
        private val TAG: String = ScanFragment::class.java.simpleName
        private val CAMERAX_PERMISSIONS = arrayOf(
            android.Manifest.permission.CAMERA
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentScanBinding.inflate(inflater, container, false)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Ask for camera permission
        if (!hasRequiredPermissions()) {
            checkCameraPermission()
        } else {
            startCamera()
        }

        initializeRotationAlertDialog()

        // sensors for rotation check
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        rotationVectorSensor?.let {
            sensorManager.unregisterListener(this)
        }
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
            it.targetRotation = Surface.ROTATION_0
            it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        }

        // image capture use case
        imageCapture = ImageCapture.Builder()
            .setTargetResolution(Size(224, 224))
            .setTargetRotation(Surface.ROTATION_0)
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
            ImageSavedCallback(requireContext())
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type === Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // orientationAngles[0]: Azimuth (rotation around the z-axis)
            // orientationAngles[1]: Pitch (rotation around the x-axis)
            // orientationAngles[2]: Roll (rotation around the y-axis)

            val pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()

            if (pitch > -65) {
                showRotationAlertDialog()
            }
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
       // NOTHING TO OD
    }

    private fun showRotationAlertDialog() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    private fun initializeRotationAlertDialog() {
        // Create an AlertDialog.Builder instance
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())

        // Configure the dialog's title, message, and button
        context?.let {
            builder.setTitle(it.resources.getString(R.string.rotation_alert_title))
            builder.setMessage(it.resources.getString(R.string.rotation_alert_message))
            builder.setPositiveButton(it.resources.getString(R.string.rotation_alert_positive_button_text),
                DialogInterface.OnClickListener { dialog, which ->
                    // Handle the OK button action here
                })
            builder.setNegativeButton(it.resources.getString(R.string.rotation_alert_negative_button_text), DialogInterface.OnClickListener { dialogInterface, i ->
                requireActivity().onBackPressed()
            })

            // Create and show the dialog
            dialog = builder.create()
        }
    }
}