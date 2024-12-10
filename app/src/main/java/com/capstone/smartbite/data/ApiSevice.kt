package com.capstone.smartbite.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @GET("events")
    fun getEventsactive(
        @Query("active") active: Int
    ): Call<EventResponseList>

    @GET("events/{id}")
    fun getDetailEvent(@Path("id") id: String
    ): Call<EventDetailResponse>

}