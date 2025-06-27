package com.example.paintingrecognition.views.main.fragments

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.paintingrecognition.R
import com.example.paintingrecognition.adapters.ScanResultsAdapter
import com.example.paintingrecognition.databinding.FragmentScanResultBinding
import com.example.paintingrecognition.models.CapturedImage
import com.example.paintingrecognition.models.ScanResult
import com.example.paintingrecognition.utils.IOnBackPressed
import com.example.paintingrecognition.viewModels.CapturedImageViewModel
import com.example.paintingrecognition.viewModels.ScanViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale


class ScanResultFragment(private val scanViewModel: ScanViewModel, private val capturedImageViewModel: CapturedImageViewModel, private val capturedImage: CapturedImage) : Fragment(),
    IOnBackPressed {

    private lateinit var binding: FragmentScanResultBinding

    private lateinit var scanResultsAdapter: ScanResultsAdapter
    private var scanDisposable: Disposable? = null

    private var tflite: Interpreter? = null

    // Your class labels (replace with your actual painting classes)
    private val classLabels = arrayOf(
        "AbstractExpressionism",
        "Baroque",
        "Cubism",
        "Expressionism",
        "Fauvism",
        "Impressionism",
        "Minimalism",
        "NaiveArtPrimitivism",
        "PopArt",
        "Realism",
        "Renaissance",
        "Rococo",
        "Romanticism",
        "Symbolism",
        "AI_Generated"
    )

    private var model: com.example.paintingrecognition.ml.Model? = null


    companion object {
        private val TAG: String = ScanResultFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScanResultBinding.inflate(inflater, container, false)

        setUpScanResultRecyclerView()
        registerForScanChanges()

        try {
            model = com.example.paintingrecognition.ml.Model.newInstance(requireContext())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        loadImageToView()
        evaluateImageFromUri(Uri.parse(capturedImage.path), classLabels.toList())

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

    private fun resizeBitmap(bitmap: Bitmap, size: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }

    override fun onBackPressed(): Boolean {
        return scanResultsAdapter.onBackPressed()
    }

    private fun evaluateImageFromUri(imageUri: Uri, labels: List<String>) {
            val context = requireContext()

            // Load and resize image to 224x224
            val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            val safeBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            // Convert bitmap to float buffer [1, 224, 224, 3]
            val byteBuffer = convertBitmapToByteBuffer(safeBitmap)

            // Wrap byteBuffer into TensorBuffer
            val inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), org.tensorflow.lite.DataType.FLOAT32)
            inputFeature.loadBuffer(byteBuffer)

            // Load model and run inference
            val outputs = model!!.process(inputFeature)
            val outputArray = outputs.outputFeature0AsTensorBuffer.floatArray


            // Match predictions to labels
            val result = labels.zip(outputArray.toList())
                .sortedByDescending { it.second }
                .joinToString("\n") { (label, score) ->
                    "$label: ${"%.2f".format(Locale.US, score * 100)}%"
                }

            Log.d("RESULTS", result)

            val items = labels.zip(outputArray.toList())
                .sortedByDescending { it.second }
                .take(5)
                .map {
                    ScanResult(null, capturedImage.name, it.first, it.second*100, capturedImage.path)
                }
                .toMutableList()

            scanViewModel.emmitResult(items)

            model!!.close()
        }


        private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
            val inputSize = 224
            val bytePerChannel = 4 // float32 = 4 bytes
            val bufferSize = inputSize * inputSize * 3 * bytePerChannel
            val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(inputSize * inputSize)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            for (pixel in intValues) {
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                byteBuffer.putFloat(r)
                byteBuffer.putFloat(g)
                byteBuffer.putFloat(b)
            }

            return byteBuffer
        }
}
