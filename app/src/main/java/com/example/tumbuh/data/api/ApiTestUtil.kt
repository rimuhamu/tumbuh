package com.example.tumbuh.data.api

import android.content.Context
import android.util.Log
import com.example.tumbuh.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApiTestUtil {
    private const val TAG = "ApiTestUtil"

    suspend fun validateApiSetup(context: Context): ApiValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                // Check API key format
                val apiKey = BuildConfig.PLANTNET_API_KEY
                if (!ApiService.isValidApiKey(apiKey)) {
                    return@withContext ApiValidationResult.InvalidApiKey
                }

                // Check network connectivity
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    return@withContext ApiValidationResult.NoNetwork
                }

                // Test API endpoint accessibility
                val testResult = testApiEndpoint()
                return@withContext testResult

            } catch (e: Exception) {
                Log.e(TAG, "API validation failed", e)
                ApiValidationResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun testApiEndpoint(): ApiValidationResult {
        return try {
            val client = ApiService.plantNetApi
            ApiValidationResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "API endpoint test failed", e)
            ApiValidationResult.Error("Cannot reach PlantNet API: ${e.message}")
        }
    }

    fun getApiKeyInstructions(): String {
        return """
            To get your PlantNet API key:
            
            1. Go to https://my.plantnet.org/
            2. Create a free account
            3. Navigate to "My API keys"
            4. Create a new API key
            5. Copy the key and replace it in ApiService.kt
            
            Free tier includes 500 requests per day.
        """.trimIndent()
    }
}

sealed class ApiValidationResult {
    object Success : ApiValidationResult()
    object InvalidApiKey : ApiValidationResult()
    object NoNetwork : ApiValidationResult()
    data class Error(val message: String) : ApiValidationResult()
}

// Simple network utility
object NetworkUtil {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? android.net.ConnectivityManager
        val activeNetwork = connectivityManager?.activeNetwork
        val networkCapabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null
    }
}
