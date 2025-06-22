package com.hsmile.cheese321.data.photobooth.repository.dto

data class PhotoBoothSearchCriteria(
    val lat: Double,
    val lng: Double,
    val radius: Int? = null,
    val region: String? = null,
    val brand: String? = null
)