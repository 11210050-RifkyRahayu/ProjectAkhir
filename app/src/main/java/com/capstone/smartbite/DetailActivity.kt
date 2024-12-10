package com.capstone.smartbite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.capstone.smartbite.data.ApiConfig
import com.capstone.smartbite.data.Event
import com.capstone.smartbite.data.EventDetailResponse
import com.capstone.smartbite.databinding.ActivityDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getIntExtra("EXTRA_EVENT_ID", 0)
        if (eventId != 0) {
            fetchEventDetail(eventId)
        } else {
            showLoading(false)
        }
    }

    private fun fetchEventDetail(eventId: Int) {
        showLoading(true)
        val client = ApiConfig.getApiService().getDetailEvent(eventId.toString())
        client.enqueue(object : Callback<EventDetailResponse> {
            override fun onResponse(call: Call<EventDetailResponse>, response: Response<EventDetailResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    response.body()?.event?.let { event ->
                        updateUI(event)
                    }
                } else {
                }
            }

            override fun onFailure(call: Call<EventDetailResponse>, t: Throwable) {
                showLoading(false)
            }
        })
    }

    private fun updateUI(event: Event) {
        Glide.with(this).load(event.mediaCover).into(binding.imageView)
        binding.tvName.text = event.name
        binding.tvOwner.text = event.ownerName
        binding.tvLocation.text = event.cityName
        binding.tvQuota.text = "${event.quota - event.registrants}"
        binding.tvCategory.text = event.category
        binding.tvDescription.text = HtmlCompat.fromHtml(event.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvBegin.text = event.beginTime

        binding.btnRegist.setOnClickListener {
            val web = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
            startActivity(web)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
