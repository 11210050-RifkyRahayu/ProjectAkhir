package com.capstone.smartbite.ui.Kamera

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.smartbite.R
import com.capstone.smartbite.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let {
            Uri.parse(it)
        }
        val result = intent.getStringExtra(EXTRA_RESULT)

        imageUri?.also {uri ->
            Log.d("Image URI", "showImage from uri: $uri")
            binding.resultImage.setImageURI(uri)
        } ?: Log.e("ResultActivity", "Image URI is null")

        result?.also {text ->
            Log.d("Result", "showResult Text: $text")
            binding.resultText.text = text
        } ?: Log.e("ResultActivity", "Result text is null")
    }
    // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}