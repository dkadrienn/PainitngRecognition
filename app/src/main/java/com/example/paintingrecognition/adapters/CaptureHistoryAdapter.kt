package com.example.paintingrecognition.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paintingrecognition.MainActivity
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databinding.CaptureHistoryItemBinding
import com.example.paintingrecognition.models.CapturedImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaptureHistoryAdapter(var captureResults: List<CapturedImage>, private val context: Context?): RecyclerView.Adapter<CaptureHistoryAdapter.CaptureItemViewHolder>() {

    lateinit var captureHistoryBinding: CaptureHistoryItemBinding


    inner class CaptureItemViewHolder(view: View): RecyclerView.ViewHolder(view)  {
        val captureImageView = captureHistoryBinding.captureImageVew
        val captureName = captureHistoryBinding.captureName
        val captureTime = captureHistoryBinding.captureTime
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaptureItemViewHolder {
        captureHistoryBinding = CaptureHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CaptureItemViewHolder(captureHistoryBinding.root)
    }

    override fun getItemCount(): Int {
        return captureResults.size
    }

    override fun onBindViewHolder(holder: CaptureItemViewHolder, position: Int) {

        holder.itemView.setOnClickListener {
            MainActivity.navigation.openImageResultDetailPage(captureResults[position], null)
        }


        holder.captureName.text = captureResults[position].name

        val date = Date(captureResults[position].creationTimestamp)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val formattedDate = simpleDateFormat.format(date)

        holder.captureTime.text = formattedDate

        context?.let {
            val bitmap2 = MediaStore.Images.Media.getBitmap(it.contentResolver, Uri.parse(captureResults[position].path))
            it.contentResolver.openInputStream(Uri.parse(captureResults[position].path))?.use { inputStream ->
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

                Glide.with(it).clear(holder.captureImageView)

                Glide.with(it)
                    .load(newBitmap)
                    .placeholder(R.drawable.logobg)
                    .override(1000, 1000)
                    .centerCrop()
                    .into(holder.captureImageView)
            }
        }
    }

}