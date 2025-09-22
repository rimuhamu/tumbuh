package com.example.tumbuh.data.repository

import android.content.Context
import android.net.Uri
import com.example.tumbuh.BuildConfig
import com.example.tumbuh.data.api.ApiService
import com.example.tumbuh.data.models.PlantIdentification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PlantRepository {

    private val apiService = ApiService.plantNetApi

    suspend fun identifyPlant(imageUri: Uri, context: Context): Result<List<PlantIdentification>> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert URI to file for API upload
                val tempFile = createTempImageFile(imageUri, context)

                // Prepare multipart request
                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("images", tempFile.name, requestFile)
                val organsPart = "leaf".toRequestBody("text/plain".toMediaTypeOrNull())

                // Make API call
                val response = apiService.identifyPlant(
                    images = imagePart,
                    organs = MultipartBody.Part.createFormData("organs", "leaf"),
                    apiKey = BuildConfig.PLANTNET_API_KEY
                )

                if (response.isSuccessful) {
                    val plantNetResponse = response.body()
                    val identifications = plantNetResponse?.results?.map { result ->
                        PlantIdentification(
                            id = generateId(),
                            scientificName = result.species.scientificName,
                            commonName = result.species.commonNames?.firstOrNull(),
                            confidence = result.score,
                            family = result.species.family?.scientificName,
                            genus = result.species.genus?.scientificName,
                            imageUrl = result.images.firstOrNull()?.url?.medium
                        )
                    } ?: emptyList()

                    // Clean up temp file
                    tempFile.delete()

                    Result.success(identifications)
                } else {
                    tempFile.delete()
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid image format or request"
                        401 -> "Invalid API key"
                        402 -> "API quota exceeded"
                        404 -> "Plant not found in database"
                        500 -> "Server error"
                        else -> "API Error: ${response.code()}"
                    }
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun createTempImageFile(imageUri: Uri, context: Context): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val tempFile = File(context.cacheDir, "temp_plant_${System.currentTimeMillis()}.jpg")

        FileOutputStream(tempFile).use { output ->
            inputStream?.copyTo(output)
        }
        inputStream?.close()

        return tempFile
    }

    private fun generateId(): String {
        return "plant_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
