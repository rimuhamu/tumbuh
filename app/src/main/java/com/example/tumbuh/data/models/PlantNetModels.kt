package com.example.tumbuh.data.models

import com.google.gson.annotations.SerializedName

data class PlantNetResponse(
    @SerializedName("results")
    val results: List<PlantResult>,
    @SerializedName("query")
    val query: PlantQuery,
    @SerializedName("remainingIdentificationRequests")
    val remainingRequests: Int
)

data class PlantResult(
    @SerializedName("score")
    val score: Double,
    @SerializedName("species")
    val species: Species,
    @SerializedName("images")
    val images: List<PlantImage>
)

data class Species(
    @SerializedName("scientificNameWithoutAuthor")
    val scientificName: String,
    @SerializedName("scientificNameAuthorship")
    val authorship: String?,
    @SerializedName("genus")
    val genus: Genus?,
    @SerializedName("family")
    val family: Family?,
    @SerializedName("commonNames")
    val commonNames: List<String>?
)

data class Genus(
    @SerializedName("scientificNameWithoutAuthor")
    val scientificName: String
)

data class Family(
    @SerializedName("scientificNameWithoutAuthor")
    val scientificName: String
)

data class PlantImage(
    @SerializedName("url")
    val url: UrlInfo
)

data class UrlInfo(
    @SerializedName("o")
    val original: String,
    @SerializedName("m")
    val medium: String,
    @SerializedName("s")
    val small: String
)

data class PlantQuery(
    @SerializedName("project")
    val project: String,
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("modifiers")
    val modifiers: List<String>,
    @SerializedName("organs")
    val organs: List<String>
)

// UI Domain Model
data class PlantIdentification(
    val id: String = "",
    val scientificName: String,
    val commonName: String?,
    val confidence: Double,
    val family: String?,
    val genus: String?,
    val imageUrl: String?,
    val authorship: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
){
    val displayName: String
        get() = commonName?.takeIf { it.isNotBlank() } ?: scientificName

    val confidencePercentage: Int
        get() = (confidence * 100).toInt()

    val isHighConfidence: Boolean
        get() = confidence >= 0.7

    val confidenceLevel: String
        get() = when {
            confidence >= 0.9 -> "Very High"
            confidence >= 0.7 -> "High"
            confidence >= 0.5 -> "Medium"
            confidence >= 0.3 -> "Low"
            else -> "Very Low"
        }
}
