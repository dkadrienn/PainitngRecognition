package com.example.paintingrecognition

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paintingrecognition.adapters.CaptureHistoryAdapter
import com.example.paintingrecognition.databinding.FragmentHistoryBinding
import com.example.paintingrecognition.eventInterfaces.CapturedImageEvent
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import kotlinx.coroutines.launch

class HistoryFragment(private val capturedImageViewModel: CapturedImageViewModel) : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var captureHistoryAdapter: CaptureHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        setUpScanResultRecyclerView()
        registerForScanChanges()

        return binding.root
    }

    private fun setUpScanResultRecyclerView() {
        captureHistoryAdapter = CaptureHistoryAdapter(mutableListOf(), context)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.adapter = captureHistoryAdapter
    }

    private fun registerForScanChanges() {
        lifecycleScope.launch {
            context?.let { innerContext ->
                capturedImageViewModel._capturedImages.collect {
                    var filteredItems = it.filter { capturedImage ->
                        // filter elements which are deleted from phone but still present in DB, also deletes them from db
                        if (!doesFileExist(innerContext, Uri.parse(capturedImage.path))) {
                            capturedImageViewModel.onEvent(CapturedImageEvent.DeleteCapturedImage(capturedImage))
                            return@filter false
                        }
                        return@filter true
                    }

                    binding.profileTitleTextView.text = getString(R.string.profile_title_text, filteredItems.size)
                    binding.profileTitleTextView.visibility = View.VISIBLE

                    capturedImageViewModel.loadedCapturedImages = filteredItems
                    captureHistoryAdapter.captureResults = it
                    captureHistoryAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun doesFileExist(context: Context, fileUri: Uri?): Boolean {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            // Use appropriate projection for your content type. This example is for images.
            cursor = context.contentResolver.query(fileUri!!, projection, null, null, null)
            cursor != null && cursor.moveToFirst()
        } finally {
            cursor?.close()
        }
    }
}