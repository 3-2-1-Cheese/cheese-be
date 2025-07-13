package com.hsmile.cheese321.api.photobooth.controller

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothRecommendationService
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothService
import com.hsmile.cheese321.api.photobooth.spec.PhotoBoothApi
import com.hsmile.cheese321.api.user.dto.FavoriteToggleResponse
import com.hsmile.cheese321.api.user.service.FavoriteService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RestController

/**
 * 사진관 정보 컨트롤러
 */
@RestController
class PhotoBoothController(
    private val photoBoothService: PhotoBoothService,
    private val favoriteService: FavoriteService,
    private val recommendationService: PhotoBoothRecommendationService
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
        return photoBoothService.getPhotoBooths(userId, lat, lng, radius, region, brand, keyword)
    }

    /**
     * 사진관 상세 조회
     */
    override fun getPhotoBoothDetail(
        @AuthenticationPrincipal userId: String,
        id: String
    ): PhotoBoothDetailResponse {
        return photoBoothService.getPhotoBoothDetail(id, userId)
    }

    /**
     * 사진관 찜하기/취소 토글
     */
    override fun toggleFavorite(
        @AuthenticationPrincipal userId: String,
        id: String
    ): ResponseEntity<FavoriteToggleResponse> {
        val response = favoriteService.toggleFavorite(userId, id)
        return ResponseEntity.ok(response)
    }

    /**
     * AI 기반 추천 사진관 목록
     */
    override suspend fun getRecommendedPhotoBooths(
        @AuthenticationPrincipal userId: String,
        lat: Double?,
        lng: Double?,
        limit: Int
    ): ResponseEntity<List<PhotoBoothResponse>> {
        val recommendations = recommendationService.getRecommendedPhotoBooths(userId, lat, lng, limit)
        return ResponseEntity.ok(recommendations)
    }
}