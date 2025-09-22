package com.example.tumbuh.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiService {
    private const val BASE_URL = "https://my-api.plantnet.org/"

    fun isValidApiKey(apiKey: String): Boolean {
        if (apiKey.isBlank()) {
            return false
        }

        if (apiKey == "YOUR_API_KEY_HERE") {
            return false
        }

        if (apiKey.length < 20 || apiKey.length > 50) {
            return false
        }


        val validPattern = Regex("^[a-zA-Z0-9._-]+$")
        if (!apiKey.matches(validPattern)) {
            return false
        }

        return true
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val plantNetApi: PlantNetApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlantNetApi::class.java)
    }
}
