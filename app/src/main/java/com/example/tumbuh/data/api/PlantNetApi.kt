package com.example.tumbuh.data.api

import com.example.tumbuh.data.models.PlantNetResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PlantNetApi {
    @Multipart
    @POST("/v1/identify/{project}")
    suspend fun identifyPlant(
        @Path("project") project: String = "k-world-flora",
        @Part images: MultipartBody.Part,
        @Part("organs") organs: MultipartBody.Part,
        @Query("api-key") apiKey: String,
        @Query("include-related-images") includeImages: Boolean = true,
        @Query("no-reject") noReject: Boolean = false,
        @Query("nb-results") nbResults: Int = 10,
        @Query("lang") lang: String = "en"
    ): Response<PlantNetResponse>
}

