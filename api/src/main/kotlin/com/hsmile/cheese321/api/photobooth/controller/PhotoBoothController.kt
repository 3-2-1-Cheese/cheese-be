package com.hsmile.cheese321.api.photobooth.controller

import com.hsmile.cheese321.api.photobooth.dto.*
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothService
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothRecommendationService
import com.hsmile.cheese321.api.photobooth.service.RatingService
import com.hsmile.cheese321.api.photobooth.spec.PhotoBoothApi
import com.hsmile.cheese321.api.user.dto.FavoriteToggleResponse
import com.hsmile.cheese321.api.user.service.FavoriteService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * 사진관 정보 컨트롤러
 */
@RestController
class PhotoBoothController(
    private val photoBoothService: PhotoBoothService,
    private val recommendationService: PhotoBoothRecommendationService,
    private val favoriteService: FavoriteService,
    private val ratingService: RatingService
) : PhotoBoothApi {

    /**
     * 사진관 목록 조회
     */
    override fun getPhotoBooths(
        @AuthenticationPrincipal userId: String,
        lat: Double?,
        lng: Double?,
        radius: Int?,
        region: String?,
        brand: String?,
        keyword: String?
    ): List<PhotoBoothResponse> {
        return photoBoothService.getPhotoBooths(
            userId = userId,
            lat = lat,
            lng = lng,
            radius = radius,
            region = region,
            brand = brand,
            keyword = keyword
        )
    }

    /**
     * 사진관 상세 정보 조회
     */
    override fun getPhotoBoothDetail(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): PhotoBoothDetailResponse {
        return photoBoothService.getPhotoBoothDetail(id, userId)
    }

    /**
     * 사진관 찜하기/취소
     */
    override fun toggleFavorite(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): ResponseEntity<FavoriteToggleResponse> {
        val response = favoriteService.toggleFavorite(userId, id)
        return ResponseEntity.ok(response)
    }

    /**
     *  추천 사진관 목록
     */
    override fun getRecommendedPhotoBooths(
        @AuthenticationPrincipal userId: String,
        lat: Double?,
        lng: Double?,
        limit: Int
    ): ResponseEntity<List<PhotoBoothResponse>> {
        val recommendations = runBlocking {
            recommendationService.getRecommendedPhotoBooths(userId, lat, lng, limit)
        }
        return ResponseEntity.ok(recommendations)
    }

    // ===== 별점 관련 API =====

    /**
     * 사진관 별점 등록/수정
     */
    override fun upsertRating(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String,
        @Valid @RequestBody request: RatingRequest
    ): ResponseEntity<RatingResponse> {
        val response = ratingService.upsertRating(userId, id, request.rating)
        return ResponseEntity.ok(response)
    }

    /**
     * 사진관 별점 삭제
     */
    override fun deleteRating(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): ResponseEntity<RatingDeleteResponse> {
        val response = ratingService.deleteRating(userId, id)
        return ResponseEntity.ok(response)
    }
}