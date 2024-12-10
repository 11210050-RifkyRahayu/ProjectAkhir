package com.capstone.smartbite.ui.Kamera

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.capstone.smartbite.R
import com.capstone.smartbite.databinding.FragmentCameraBinding
import com.dicoding.asclepius.MainModel.MainViewModel
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File
import java.text.NumberFormat

class CameraFragment : Fragment(), ImageClassifierHelper.ClassifierListener {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var viewModel: MainViewModel

    private var currentImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        imageClassifierHelper = ImageClassifierHelper(context = requireContext(), classifierListener = this)
        binding.cardcamera.setOnClickListener { startCamera() }
        binding.cardgaleri.setOnClickListener { startGallery() }
        binding.buttonAnalisa.setOnClickListener { analyzeImage() }

        viewModel.imageUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                binding.gambaruplode.setImageURI(it)
                binding.buttonAnalisa.visibility = View.VISIBLE
            } ?: run {
                binding.gambaruplode.setImageResource(R.drawable.baseline_image_24)
                binding.buttonAnalisa.visibility = View.GONE
            }
        }

        return binding.root
    }

    private fun startCamera() {
        // Panggil Utils untuk mendapatkan URI untuk gambar dari kamera
        currentImageUri = Utils().getImageUri(requireContext())
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            // Setelah pengambilan gambar berhasil, simpan URI gambar ke ViewModel
            viewModel.setImageUri(currentImageUri)

            // Tampilkan gambar hasil pengambilan kamera
            showImage()

            // Lakukan analisis gambar setelah gambar disimpa
        } else {
            currentImageUri = null
            showToast("Pengambilan gambar gagal")
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        val chooser = Intent.createChooser(intent, "Pilih gambar")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                viewModel.setImageUri(selectedImg)
                currentImageUri = uri
                startCrop(selectedImg)
            }
        }
    }

    private fun startCrop(imageUri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))

        val uCrop = UCrop.of(imageUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(224, 224)
            .getIntent(requireContext())

        cropImageResultLauncher.launch(uCrop)
    }

    private val cropImageResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let {
                viewModel.currentImageUri = it
                showImage()
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.let { showToast("Ada kesalahan crop gambar: ${it.message}") }
        } else if (result.resultCode == RESULT_CANCELED) {
            showToast("Crop dibatalkan")
            viewModel.setImageUri(null)
            binding.gambaruplode.setImageResource(R.drawable.baseline_image_24)
            binding.buttonAnalisa.visibility = View.GONE
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.gambaruplode.setImageURI(it)
            binding.buttonAnalisa.visibility = View.VISIBLE
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let {
            binding.progressIndicator.visibility = View.VISIBLE
            imageClassifierHelper.classifyStaticImage(it)
        } ?: showToast("Tidak ada gambar yang dipilih")
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        binding.progressIndicator.visibility = View.GONE

        if (results.isNullOrEmpty()) {
            showToast("Klasifikasi gagal: Tidak ada klasifikasi yang ditemukan")
            return
        }

        val category = results.firstOrNull()?.categories?.firstOrNull()

        val resultText = category?.let {
            val scoreText = NumberFormat.getPercentInstance().format(it.score)
            val labelText = it.label
            "Hasil deteksi kanker adalah: $labelText $scoreText"
        } ?: "Tidak Dikenali"

        moveToResult(resultText)
    }

    override fun onError(error: String) {
        binding.progressIndicator.visibility = View.GONE
        showToast("Terjadi kesalahan: $error")
    }

    private fun moveToResult(result: String) {
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
            putExtra(ResultActivity.EXTRA_RESULT, result)
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
