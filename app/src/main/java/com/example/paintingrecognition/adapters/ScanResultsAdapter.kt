package com.example.paintingrecognition.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.paintingrecognition.R
import com.example.paintingrecognition.databinding.ScanResultItemBinding
import com.example.paintingrecognition.models.ScanResult


class ScanResultsAdapter(var scanResults: MutableList<ScanResult>, private val context: Context?):
    RecyclerView.Adapter<ScanResultsAdapter.ScanResultViewHolder>() {

    lateinit var scanResultBinding: ScanResultItemBinding

    inner class ScanResultViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val scanImageView = scanResultBinding.scanResultImageView
        val scanResultName = scanResultBinding.scanResultName
        val scanResultResemblance = scanResultBinding.scanResultResemblance
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultViewHolder {
        scanResultBinding = ScanResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ScanResultViewHolder(scanResultBinding.root)
    }

    override fun getItemCount(): Int {
        return scanResults.size
    }

    override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
        holder.scanResultName.text = scanResults[position].title
        holder.scanResultResemblance.text = scanResults[position].resemblance.toString() + "%"

        /// sets the progression to the item
        val screenWidth = context?.resources?.displayMetrics?.widthPixels
        val screenDensity = context?.resources?.displayMetrics?.density
        screenWidth?.let { width ->
            screenDensity?.let { density ->
                // 40 is the margin start and end of the scan_result_item
                val newWidth = screenWidth - (40 * density)
                val params: ViewGroup.LayoutParams = scanResultBinding.progressCardView.layoutParams
                params.width = (newWidth * (scanResults[position].resemblance / 100)).toInt()

                scanResultBinding.progressCardView.layoutParams = params

                context?.resources?.let {
                    if (scanResults[position].resemblance < 50) {
                        scanResultBinding.progressCardView.backgroundTintList = ColorStateList.valueOf(it.getColor(R.color.progressLow))
                    } else {
                        scanResultBinding.progressCardView.backgroundTintList = ColorStateList.valueOf(it.getColor(R.color.progressHeigh))
                    }
                }
            }

            // the last item needs padding to be above the bottom menu
            if (position == scanResults.size - 1) {
                scanResultBinding.root.setPadding(0,0,0,500)
            }
        }


        context?.let {
            Glide.with(it)
                .load(scanResults[position].imageUrl)
                .placeholder(R.drawable.logobg)
                .centerCrop()
                .into(holder.scanImageView)
        }
    }
}