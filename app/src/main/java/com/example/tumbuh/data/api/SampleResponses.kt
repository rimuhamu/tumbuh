package com.example.tumbuh.data.api

import com.example.tumbuh.data.models.*

object SampleResponses {

    fun getSamplePlantIdentification(): List<PlantIdentification> {
        return listOf(
            PlantIdentification(
                id = "sample_1",
                scientificName = "Rosa rubiginosa",
                commonName = "Sweet Briar",
                confidence = 0.89,
                family = "Rosaceae",
                genus = "Rosa",
                imageUrl = "https://bs.plantnet.org/image/o/12345_m.jpg",
                authorship = "L."
            ),
            PlantIdentification(
                id = "sample_2",
                scientificName = "Rosa canina",
                commonName = "Dog Rose",
                confidence = 0.76,
                family = "Rosaceae",
                genus = "Rosa",
                imageUrl = "https://bs.plantnet.org/image/o/67890_m.jpg",
                authorship = "L."
            ),
            PlantIdentification(
                id = "sample_3",
                scientificName = "Rosa multiflora",
                commonName = "Multiflora Rose",
                confidence = 0.65,
                family = "Rosaceae",
                genus = "Rosa",
                imageUrl = "https://bs.plantnet.org/image/o/11111_m.jpg",
                authorship = "Thunb."
            )
        )
    }

    fun getSampleApiResponse(): PlantNetResponse {
        return PlantNetResponse(
            results = listOf(
                PlantResult(
                    score = 0.89,
                    species = Species(
                        scientificName = "Rosa rubiginosa",
                        authorship = "L.",
                        genus = Genus(scientificName = "Rosa"),
                        family = Family(scientificName = "Rosaceae"),
                        commonNames = listOf("Sweet Briar", "Sweet-briar", "Eglantine")
                    ),
                    images = listOf(
                        PlantImage(
                            url = UrlInfo(
                                original = "https://bs.plantnet.org/image/o/12345_o.jpg",
                                medium = "https://bs.plantnet.org/image/o/12345_m.jpg",
                                small = "https://bs.plantnet.org/image/o/12345_s.jpg"
                            )
                        )
                    )
                )
            ),
            query = PlantQuery(
                project = "k-world-flora",
                images = listOf("uploaded_image.jpg"),
                modifiers = emptyList(),
                organs = listOf("leaf")
            ),
            remainingRequests = 485
        )
    }

    object ErrorMessages {
        const val INVALID_API_KEY = "Invalid API key. Please get a valid key from https://my.plantnet.org/"
        const val QUOTA_EXCEEDED = "API quota exceeded. Please try again later or upgrade your PlantNet account."
        const val NO_RESULTS = "No plants could be identified from this image. Try a clearer photo with better lighting."
        const val NETWORK_ERROR = "Network error. Please check your internet connection."
        const val SERVER_ERROR = "PlantNet server error. Please try again later."
        const val IMAGE_TOO_LARGE = "Image file is too large. Please use a smaller image (max 5MB)."
        const val INVALID_IMAGE = "Invalid image format or request. Please try a different image."
    }
}
