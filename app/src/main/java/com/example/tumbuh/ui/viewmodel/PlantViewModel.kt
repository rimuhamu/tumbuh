package com.example.tumbuh.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tumbuh.data.api.ApiTestUtil
import com.example.tumbuh.data.api.ApiValidationResult
import com.example.tumbuh.data.api.SampleResponses
import com.example.tumbuh.data.models.PlantIdentification
import com.example.tumbuh.data.repository.PlantRepository
import kotlinx.coroutines.launch

class PlantViewModel : ViewModel() {

    private val repository = PlantRepository()
    private val tag = "PlantViewModel"

    private val _identificationState = mutableStateOf<IdentificationState>(IdentificationState.Idle)
    val identificationState: State<IdentificationState> = _identificationState

    private val _capturedImageUri = mutableStateOf<Uri?>(null)
    val capturedImageUri: State<Uri?> = _capturedImageUri

    private val _apiValidationState = mutableStateOf<ApiValidationState>(ApiValidationState.NotChecked)
    val apiValidationState: State<ApiValidationState> = _apiValidationState

    fun setCapturedImage(uri: Uri) {
        _capturedImageUri.value = uri
        Log.d(tag, "Image captured: $uri")
    }

    private fun identifyPlant(imageUri: Uri, context: Context, useDemo: Boolean = false) {
        Log.d(tag, "Starting plant identification...")
        _identificationState.value = IdentificationState.Loading

        if (useDemo) {
            // Use demo data for testing UI
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000) // Simulate API delay
                _identificationState.value = IdentificationState.Success(
                    SampleResponses.getSamplePlantIdentification()
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.identifyPlant(imageUri, context)
                    .onSuccess { identifications ->
                        Log.d(tag, "Identification successful: ${identifications.size} results")
                        if (identifications.isNotEmpty()) {
                            _identificationState.value = IdentificationState.Success(identifications)
                        } else {
                            _identificationState.value = IdentificationState.Error(
                                "No plants identified. Try a clearer image with better lighting."
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e(tag, "Identification failed", error)
                        val errorMessage = when {
                            error.message?.contains("Invalid API key", ignoreCase = true) == true -> {
                                _apiValidationState.value = ApiValidationState.InvalidApiKey
                                "Invalid API key. Please configure your PlantNet API key."
                            }
                            error.message?.contains("network", ignoreCase = true) == true ->
                                "Network error. Please check your internet connection and try again."
                            error.message?.contains("quota", ignoreCase = true) == true ->
                                "Daily API limit reached. Try again tomorrow or upgrade your PlantNet account."
                            error.message?.contains("timeout", ignoreCase = true) == true ->
                                "Request timed out. Please try again."
                            else -> error.message ?: "Unknown error occurred"
                        }
                        _identificationState.value = IdentificationState.Error(errorMessage)
                    }
            } catch (e: Exception) {
                Log.e(tag, "Unexpected error during identification", e)
                _identificationState.value = IdentificationState.Error(
                    "Unexpected error: ${e.message}"
                )
            }
        }
    }

    fun validateApiSetup(context: Context) {
        Log.d(tag, "Validating API setup...")
        _apiValidationState.value = ApiValidationState.Checking

        viewModelScope.launch {
            val result = ApiTestUtil.validateApiSetup(context)
            _apiValidationState.value = when (result) {
                is ApiValidationResult.Success -> ApiValidationState.Valid
                is ApiValidationResult.InvalidApiKey -> ApiValidationState.InvalidApiKey
                is ApiValidationResult.NoNetwork -> ApiValidationState.NetworkError
                is ApiValidationResult.Error -> ApiValidationState.Error(result.message)
            }
            Log.d(tag, "API validation result: ${_apiValidationState.value}")
        }
    }

    fun retryIdentification(context: Context) {
        Log.d(tag, "Retrying identification...")
        _capturedImageUri.value?.let { uri ->
            identifyPlant(uri, context)
        }
    }

    fun clearState() {
        Log.d(tag, "Clearing states...")
        _identificationState.value = IdentificationState.Idle
        _capturedImageUri.value = null
    }

    fun clearApiValidation() {
        _apiValidationState.value = ApiValidationState.NotChecked
    }

    // Test with demo data
    fun testWithDemoData() {
        Log.d(tag, "Testing with demo data...")
        _identificationState.value = IdentificationState.Loading

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Simulate API delay
            _identificationState.value = IdentificationState.Success(
                SampleResponses.getSamplePlantIdentification()
            )
        }
    }

    fun getApiKeyInstructions(): String {
        return ApiTestUtil.getApiKeyInstructions()
    }
}

sealed class IdentificationState {
    data object Idle : IdentificationState()
    data object Loading : IdentificationState()
    data class Success(val identifications: List<PlantIdentification>) : IdentificationState()
    data class Error(val message: String) : IdentificationState()
}

sealed class ApiValidationState {
    data object NotChecked : ApiValidationState()
    data object Checking : ApiValidationState()
    data object Valid : ApiValidationState()
    data object InvalidApiKey : ApiValidationState()
    data object NetworkError : ApiValidationState()
    data class Error(val message: String) : ApiValidationState()
}
