package com.example.paintingrecognition.views.main.fragments

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databinding.FragmentImageResultDetailPageBinding
import com.example.paintingrecognition.databases.events.ScanResultEvent
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.viewModels.ScanViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageResultDetailPage(private val capturedImage: CapturedImage, private val scanResult: ScanResult?, private val scanViewModel: ScanViewModel) : Fragment() {

    private lateinit var binding: FragmentImageResultDetailPageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageResultDetailPageBinding.inflate(inflater, container, false)

        if (scanResult != null) {
            setUpViewsWithItems(capturedImage, scanResult)
        } else {
            loadScanResult()
        }

        return binding.root
    }

    private fun loadScanResult() {
        scanViewModel.onEvent(ScanResultEvent.GetScanResultByUrl(capturedImage.path))
        lifecycleScope.launch {
            context?.let { innerContext ->
                scanViewModel._scanResultByUrl.collect {
                    if (it != null) {
                        setUpViewsWithItems(capturedImage, it)
                    }
                }
            }
        }
    }

    private fun setUpViewsWithItems(innerCapturedImage: CapturedImage, innerScanResult: ScanResult) {
        val date = Date(innerCapturedImage.creationTimestamp)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedDate = simpleDateFormat.format(date)

        binding.capturedImagePath.text = innerCapturedImage.path
        binding.capturedImageTitle.text = innerCapturedImage.name
        binding.capturedImageTime.text = formattedDate
        binding.resultGenre.text = innerScanResult.genre
       // binding.resultPainter.text = innerScanResult.painter
        binding.resultTitle.text = innerScanResult.title
        binding.resultResemblance.text = innerScanResult.resemblance.toString() + "%"
        binding.materialCardView.translationZ = -40f
        binding.materialCardView2.translationZ = -40f


        context?.let {
            Glide.with(it).clear(binding.capturedImageView)
            Glide.with(it).clear(binding.resultImageView)

            Glide.with(it)
                .load("url")
                .placeholder(R.drawable.logobg)
                .centerCrop()
                .into(binding.capturedImageView)




            val bitmap2 = MediaStore.Images.Media.getBitmap(it.contentResolver, Uri.parse(innerCapturedImage.path))
            it.contentResolver.openInputStream(Uri.parse(innerCapturedImage.path))?.use { inputStream ->
                val exif = ExifInterface(inputStream)

                // Get the orientation
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                // Calculate the rotation needed
                val rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }

                // Rotate the bitmap
                val matrix = Matrix().apply {
                    postRotate(rotationDegrees.toFloat())
                }

                val newBitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.width, bitmap2.height, matrix, true)

                Glide.with(it)
                    .load(newBitmap)
                    .placeholder(R.drawable.logobg)
                    .centerCrop()
                    .into(binding.capturedImageView)

            }
        }
    }

}