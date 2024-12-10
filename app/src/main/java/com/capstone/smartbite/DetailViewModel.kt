package com.capstone.smartbite

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.smartbite.data.ApiConfig
import com.capstone.smartbite.data.Event
import com.capstone.smartbite.data.EventDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventDetailViewModel : ViewModel() {
    private val _eventDetail = MutableLiveData<Event>()
    val eventDetail: LiveData<Event> get() = _eventDetail

    fun fetchEventDetail(eventId: Int) {
        val response = ApiConfig.getApiService().getDetailEvent(eventId.toString())
        response.enqueue(object : Callback<EventDetailResponse> {
            override fun onResponse(
                call: Call<EventDetailResponse>,
                response: Response<EventDetailResponse>
            ) {
                if (response.isSuccessful) {
                    _eventDetail.value = response.body()?.event
                } else {
                    Log.e("DETAIL_VIEWMODEL", "onResponse: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<EventDetailResponse>, t: Throwable) {
                Log.e("DETAIL_VIEWMODEL", "onFailure: ${t.message}")
            }
        })
    }
}