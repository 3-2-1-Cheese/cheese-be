package com.hsmile.cheese321.api.photobooth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/**
 * 별점 등록/수정 요청
 */
@Schema(description = "별점 등록/수정 요청")
data class RatingRequest(
    @Schema(description = "별점 (1~5)", example = "4")
    @field:Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @field:Max(value = 5, message = "별점은 5점 이하여야 합니다")
    val rating: Int
)

/**
 * 별점 등록/수정 응답
 */
@Schema(description = "별점 등록/수정 응답")
data class RatingResponse(
    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "등록/수정된 별점")
    val rating: Int,

    @Schema(description = "응답 메시지")
    val message: String,

    @Schema(description = "해당 사진관의 새로운 평균 별점")
    val newAverageRating: Double?,

    @Schema(description = "해당 사진관의 총 별점 개수")
    val totalRatings: Int
)

/**
 * 별점 삭제 응답
 */
@Schema(description = "별점 삭제 응답")
data class RatingDeleteResponse(
    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "응답 메시지")
    val message: String,

    @Schema(description = "해당 사진관의 새로운 평균 별점")
    val newAverageRating: Double?,

    @Schema(description = "해당 사진관의 총 별점 개수")
    val totalRatings: Int
)

/**
 * 평점 요약 정보
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