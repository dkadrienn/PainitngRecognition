package com.example.paintingrecognition

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.paintingrecognition.adapters.CarouselAdapter
import com.example.paintingrecognition.databinding.FragmentHomeBinding
import com.example.paintingrecognition.models.PaintingModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs

class HomeFragment : Fragment() {

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
        val items = getImages()
        if (items.isEmpty()){
            binding.itemGroup.visibility = View.INVISIBLE
            binding.fallbackGroup.visibility = View.VISIBLE
            setupFallbackClickListener()
            return
        }

        binding.itemGroup.visibility = View.VISIBLE
        binding.fallbackGroup.visibility = View.INVISIBLE

        adapter = CarouselAdapter(items, context, carouselViewPager2)
        carouselViewPager2.adapter = adapter

        // setting how many items should be seen
        carouselViewPager2.offscreenPageLimit = 3
        // handling scroll for first item
        carouselViewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        setUpTransformerForCarousel()
    }

    /**
     * After a page in the carousel gets selected this function updates the metadatas.
     */
    private fun registerPageSelectedListener() {
        carouselViewPager2.registerOnPageChangeCallback(object: OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.genreText.text = getImages()[position].genre
                binding.painterText.text = getImages()[position].painter
                binding.nameText.text = getImages()[position].title
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
                transaction?.replace(R.id.mainContainter, ScanFragment())
                transaction?.commit()
                binding.fallbackGroup.setOnClickListener(null)
            }
        }
    }

    private fun getImages(): MutableList<PaintingModel> {
        return mutableListOf(
            PaintingModel("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 1", "action", "Leonardo"),
            PaintingModel("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 2", "drama", "michelangelo"),
            PaintingModel("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 3", "horror", "NFT"),
            PaintingModel("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 4", "drama", "Unknown"),
            PaintingModel("https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 5", "scifi", "Le"),
        )
    }
}