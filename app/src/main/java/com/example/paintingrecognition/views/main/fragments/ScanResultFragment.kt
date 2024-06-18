package com.example.paintingrecognition.views.main.fragments

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.paintingrecognition.utils.IOnBackPressed
import com.example.paintingrecognition.R
import com.example.paintingrecognition.adapters.ScanResultsAdapter
import com.example.paintingrecognition.databinding.FragmentScanResultBinding
import com.example.paintingrecognition.ml.Model.Outputs
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.Genres
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.utils.GenericConstants
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ScanResultFragment(private val scanViewModel: ScanViewModel, private val capturedImageViewModel: CapturedImageViewModel, private val capturedImage: CapturedImage) : Fragment(),
    IOnBackPressed {

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

        loadImageToView()
        evaluateImage(Uri.parse(capturedImage.path))

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

    private fun loadImageToView(){
        val bitmap2 = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, Uri.parse(capturedImage.path))

        requireContext().contentResolver.openInputStream(Uri.parse(capturedImage.path))?.use { inputStream ->

            // Rotate the bitmap
            val matrix = Matrix().apply {
                postRotate(90f)
            }

            val newBitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.width, bitmap2.height, matrix, true)

            // Resize the bitmap to 224x224 and normalize the pixel values
            val resizedBitmap = resizeBitmap(newBitmap, 224)

            Glide.with(requireContext())
                .load(resizedBitmap)
                .placeholder(R.drawable.logobg)
                .centerCrop()
                .into(binding.capturedImageView)

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

    private fun evaluateImage(path: Uri){

        val model = com.example.paintingrecognition.ml.Model.newInstance(requireContext())

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        // val byteBuffer = loadImageIntoMappedByteBuffer("content://media/external/images/media/" + timestamp)
        val byteBuffer = loadImageFromUriAndResizeToByteBuffer(requireContext(), path)

        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)

        showTopResults(outputs)

        model.close()
    }


    private fun loadImageFromUriAndResizeToByteBuffer(context: Context, contentUri: Uri): ByteBuffer {
        val contentResolver: ContentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(contentUri)
            ?: throw IOException("Unable to open image from URI: $contentUri")

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Resize the bitmap to 224x224 and normalize the pixel values
        val resizedBitmap = resizeBitmap(bitmap, GenericConstants.IMAGE_SIZE)

        // Convert the resized bitmap to ByteBuffer
        val byteBuffer = ByteBuffer.allocateDirect(GenericConstants.IMAGE_SIZE * GenericConstants.IMAGE_SIZE * 3 * 4) // 1x1 image with 3 channels (RGB) and 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())

        // Get pixel values and put them into the ByteBuffer
        val intValues = IntArray(GenericConstants.IMAGE_SIZE * GenericConstants.IMAGE_SIZE)
        resizedBitmap.getPixels(intValues, 0, GenericConstants.IMAGE_SIZE, 0, 0, GenericConstants.IMAGE_SIZE, GenericConstants.IMAGE_SIZE)
        for (pixelValue in intValues) {
            byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // R
            byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)  // G
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)          // B
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    private fun resizeBitmap(bitmap: Bitmap, size: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }

    private fun showTopResults(outputs: Outputs) {
        val outputArray = outputs.outputFeature0AsTensorBuffer.floatArray
        val indexMap = HashMap<Float, Int>()

        for (i in outputArray.indices) {
            Log.d("TOPITEMS>>","LOADEDITEMS: " + i + " " + outputArray[i])
            indexMap[outputArray[i]] = i
        }
        outputArray.sortDescending()
        val topItems = outputArray.take(GenericConstants.SCAN_RESULT_COUNT)

        val items = mutableListOf<ScanResult>()

        for (item in topItems) {
            val genre = Genres.entries.first { genre ->
                genre.ordinal == indexMap[item]
            }
            items.add(ScanResult(null, capturedImage.name, genre.name, item*100, capturedImage.path))
        }

        scanViewModel.emmitResult(items)

    }

    override fun onBackPressed(): Boolean {
        return scanResultsAdapter.onBackPressed()
    }
}