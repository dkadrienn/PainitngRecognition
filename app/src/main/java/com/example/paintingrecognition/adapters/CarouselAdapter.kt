package com.example.paintingrecognition.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.paintingrecognition.R
import com.example.paintingrecognition.models.PaintingModel
import com.makeramen.roundedimageview.RoundedImageView

class CarouselAdapter(private val images: MutableList<PaintingModel>, private val context: Context?, private val viewPager2: ViewPager2):
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        return CarouselViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.painting_item, parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        return holder.bind(images[position])
    }

    inner class CarouselViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val carouselImageView: RoundedImageView = view.findViewById(R.id.carouselImageView)

        fun bind(item: PaintingModel) {
            context?.let {
                Glide.with(it)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.logobg)
                    .centerCrop()
                    .into(carouselImageView)
            }
        }
    }
}