package com.example.paintingrecognition

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.paintingrecognition.adapters.ScanResultsAdapter
import com.example.paintingrecognition.databinding.FragmentScanResultBinding
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ScanResultFragment(private val scanViewModel: ScanViewModel, private val capturedImageViewModel: CapturedImageViewModel, private val capturedImage: CapturedImage) : Fragment() {

    private lateinit var binding: FragmentScanResultBinding

    private lateinit var scanResultsAdapter: ScanResultsAdapter
    private var scanDisposable: Disposable? = null

    companion object {
        private val TAG: String = ScanResultFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScanResultBinding.inflate(inflater, container, false)

        setUpScanResultRecyclerView()
        registerForScanChanges()

        calculateResultsForCapturedImage()

        return binding.root
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
        scanResultsAdapter = ScanResultsAdapter(mutableListOf(), context, scanViewModel, capturedImageViewModel, capturedImage)
        binding.scanRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.scanRecyclerView.adapter = scanResultsAdapter
    }

    private fun calculateResultsForCapturedImage() {
        // use calculatedImage attribute with CNN and create results
        val items = mutableListOf(
            ScanResult(null,"https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 1", "action", "Leonardo", 35.5f, capturedImage.path),
            ScanResult(null,"https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 2", "drama", "michelangelo", 93.1f, capturedImage.path),
            ScanResult(null,"https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 3", "horror", "NFT", 44.2f, capturedImage.path),
            ScanResult(null,"https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 4", "drama", "Unknown", 11.5f, capturedImage.path),
            ScanResult(null,"https://www.streetmachine.com.au/wp-content/uploads/2023/07/fast-and-furious.jpg","Fast and Furious 5", "scifi", "Le", 72.5f, capturedImage.path),
        )

        scanViewModel.emmitResult(items)
    }
}