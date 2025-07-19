package com.hsmile.cheese321.data.photobooth.repository.dto

/**
 * 평점 요약 정보 DTO
 */
data class RatingSummaryDto(
    val photoBoothId: String,
    val averageRating: Double?,
    val totalRatings: Long
) {
    companion object {
        fun empty(photoBoothId: String) = RatingSummaryDto(photoBoothId, null, 0L)
    }
}