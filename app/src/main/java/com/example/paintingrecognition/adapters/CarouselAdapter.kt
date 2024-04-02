package com.example.paintingrecognition.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paintingrecognition.MainActivity
import com.example.paintingrecognition.R
import com.example.paintingrecognition.models.CapturedImage
import com.makeramen.roundedimageview.RoundedImageView

class CarouselAdapter(private val images: List<CapturedImage>, private val context: Context?):
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        return CarouselViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.painting_item, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        images[position]?.let { capturedImage ->
            holder.itemView.setOnClickListener {
                Log.d("OPENDETAILPAGE>>","open: " + capturedImage)
                MainActivity.navigation.openImageResultDetailPage(capturedImage, null)
            }
        }

        return holder.bind(images[position])
    }

    inner class CarouselViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val carouselImageView: RoundedImageView = view.findViewById(R.id.carouselImageView)

        fun bind(item: CapturedImage) {
            // rotates the bitmap to the position how the image was taken (it gets from uri as it was rotated 90 degrees)
            context?.let {
                val bitmap2 = MediaStore.Images.Media.getBitmap(it.contentResolver, Uri.parse(item.path))
                it.contentResolver.openInputStream(Uri.parse(item.path))?.use { inputStream ->
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

                    Glide.with(it).clear(carouselImageView)

                    Glide.with(it)
                        .load(newBitmap)
                        .placeholder(R.drawable.logobg)
                        .centerCrop()
                        .into(carouselImageView)
                }
            }
        }
    }
}