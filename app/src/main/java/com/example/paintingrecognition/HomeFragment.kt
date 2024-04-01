package com.example.paintingrecognition

import android.content.Context
import android.content.res.ColorStateList
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.paintingrecognition.adapters.CarouselAdapter
import com.example.paintingrecognition.databinding.FragmentHomeBinding
import com.example.paintingrecognition.eventInterfaces.CapturedImageEvent
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.abs


class HomeFragment(private val capturedImageViewModel: CapturedImageViewModel) : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var carouselViewPager2: ViewPager2
    private lateinit var adapter: CarouselAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupCarouselRecyclerView()
        registerPageSelectedListener()
        return binding.root
    }

    private fun setupCarouselRecyclerView() {
        carouselViewPager2 = binding.viewPager2

        setUpTransformerForCarousel()

        lifecycleScope.launch {
            context?.let { innerContext ->
                getImages().collect {
                    var filteredItems = it.filter { capturedImage ->
                        // filter elements which are deleted from phone but still present in DB, also deletes them from db
                        if (!doesFileExist(innerContext, Uri.parse(capturedImage.path))) {
                            capturedImageViewModel.onEvent(CapturedImageEvent.DeleteCapturedImage(capturedImage))
                            return@filter false
                        }
                        return@filter true
                    }

                    // only present the last 5 items
                    if (filteredItems.size > 5) {
                        filteredItems = filteredItems.subList(0, 5)
                    }

                    if (filteredItems.isEmpty()){
                        binding.itemGroup.visibility = View.INVISIBLE
                        binding.fallbackGroup.visibility = View.VISIBLE
                        setupFallbackClickListener()
                        return@collect
                    }
                    capturedImageViewModel.loadedCapturedImages = filteredItems

                    binding.itemGroup.visibility = View.VISIBLE
                    binding.fallbackGroup.visibility = View.INVISIBLE

                    adapter = CarouselAdapter(filteredItems, context)
                    carouselViewPager2.adapter = adapter

                    // setting how many items should be seen
                    carouselViewPager2.offscreenPageLimit = 3
                    // handling scroll for first item
                    carouselViewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
                }
            }
        }

    }

    /**
     * After a page in the carousel gets selected this function updates the metadatas.
     */
    private fun registerPageSelectedListener() {
        carouselViewPager2.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.genreText.text = capturedImageViewModel.loadedCapturedImages[position].creationTimestamp.toString()
                binding.painterText.text = capturedImageViewModel.loadedCapturedImages[position].path
                binding.nameText.text = capturedImageViewModel.loadedCapturedImages[position].name
            }
        })
    }

    /**
     * Transformer enables the carousel to see nearby items as well.
     */
    private fun setUpTransformerForCarousel() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r: Float = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        // setting transformer for the near items to be seen as well
        carouselViewPager2.setPageTransformer(transformer)
    }

    /**
     * Pressing on the fallback message opens the ScanFragment.
     */
    private fun setupFallbackClickListener() {
        binding.fallbackGroup.setOnClickListener {
            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView?.let { view ->
                view.selectedItemId = R.id.placeholder
                // set selected color for floating button
                val floatingActionButton = activity?.findViewById<FloatingActionButton>(R.id.floatingActionButton)
                floatingActionButton?.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.primaryAccent))

                val transaction = activity?.supportFragmentManager?.beginTransaction();
                transaction?.replace(R.id.mainContainter, ScanFragment(capturedImageViewModel::onEvent))
                transaction?.commit()
                binding.fallbackGroup.setOnClickListener(null)
            }
        }
    }

    private fun getImages(): Flow<List<CapturedImage>> {
        return capturedImageViewModel._capturedImages
    }

    fun doesFileExist(context: Context, fileUri: Uri?): Boolean {
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