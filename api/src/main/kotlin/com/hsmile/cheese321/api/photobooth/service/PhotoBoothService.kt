package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.*
import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothQueryRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRatingRepository
import com.hsmile.cheese321.data.photobooth.repository.dto.RatingSummaryDto
import com.hsmile.cheese321.data.photobooth.exception.PhotoBoothNotFoundException
import com.hsmile.cheese321.data.user.repository.UserRepository
import com.hsmile.cheese321.data.user.repository.UserFavoritePhotoBoothRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import kotlin.math.*

/**
 * 사진관 비즈니스 로직 처리 서비스
 */
@Service
@Transactional(readOnly = true)
class PhotoBoothService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothQueryRepository: PhotoBoothQueryRepository,
    private val photoBoothRatingRepository: PhotoBoothRatingRepository,
    private val userRepository: UserRepository,
    private val userFavoritePhotoBoothRepository: UserFavoritePhotoBoothRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * 위치 기반 사진관 검색
     */
    fun getPhotoBooths(
        userId: String,
        lat: Double?,
        lng: Double?,
        radius: Int?,
        region: String?,
        brand: String?,
        keyword: String?,
        sort: String = "distance"
    ): List<PhotoBoothResponse> {
        val photoBooths = if (lat != null && lng != null) {
            val searchRadius = radius ?: 1000
            photoBoothQueryRepository.search(lat, lng, searchRadius, region, brand, keyword)
        } else {
            photoBoothQueryRepository.searchWithoutLocation(region, brand, keyword)
        }

        if (photoBooths.isEmpty()) return emptyList()

        // 개인화/평점 정보를 한번에 조회
        val boothIds = photoBooths.map { it.id }
        val userPreferredKeywords = getUserPreferredKeywords(userId)
        val favoriteBoothIds = userFavoritePhotoBoothRepository.findFavoritePhotoBoothIds(userId, boothIds).toSet()

        // DTO 방식으로 평점 정보 일괄 조회
        val ratingSummaries = photoBoothRatingRepository.findRatingSummaries(boothIds)
            .associateBy { it.photoBoothId }

        return photoBooths.map { booth ->
            val ratingSummary = ratingSummaries[booth.id]
            // 공개 메서드 사용으로 변경
            convertToResponse(
                photoBooth = booth,
                userLat = lat,
                userLng = lng,
                userPreferredKeywords = userPreferredKeywords,
                isFavorite = favoriteBoothIds.contains(booth.id),
                totalRatings = ratingSummary?.totalRatings?.toInt() ?: 0
            )
        }
    }

    /**
     * 사진관 상세 정보 조회 (개인화 정보 + 평점 포함)
     */
    fun getPhotoBoothDetail(id: String, userId: String): PhotoBoothDetailResponse {
        val photoBooth = photoBoothRepository.findByIdOrNull(id)
            ?: throw PhotoBoothNotFoundException("PhotoBooth not found with id: $id")

        // 개인화/평점 정보를 각각 조회 (상세 조회의 경우 여러번 쿼리도 괜찮음)
        val userPreferredKeywords = getUserPreferredKeywords(userId)
        val isFavorite = userFavoritePhotoBoothRepository.existsByUserIdAndPhotoBoothId(userId, id)

        // DTO 방식으로 평점 정보 조회
        val ratingSummary = photoBoothRatingRepository.findRatingSummary(id)
        val userRating = photoBoothRatingRepository.findByUserIdAndPhotoBoothId(userId, id)?.rating

        return photoBooth.toDetailResponse(userPreferredKeywords, isFavorite, ratingSummary, userRating)
    }

    /**
     * PhotoBooth Entity를 목록용 Response DTO로 변환 (공개 메서드)
     */
    fun convertToResponse(
        photoBooth: com.hsmile.cheese321.data.photobooth.entity.PhotoBooth,
        userLat: Double?,
        userLng: Double?,
        userPreferredKeywords: List<String>,
        isFavorite: Boolean,
        totalRatings: Int
    ): PhotoBoothResponse {
        val distance = if (userLat != null && userLng != null) {
            calculateDistance(userLat, userLng, photoBooth.location.y, photoBooth.location.x).toInt()
        } else 0

        val imageUrlList = parseImageUrls(photoBooth.imageUrls)
        val keywords = getSimpleKeywords(userPreferredKeywords)
        val isRecommended = keywords.any { it.isUserPreferred }

        return PhotoBoothResponse(
            id = photoBooth.id,
            name = photoBooth.name,
            brand = photoBooth.brand,
            region = photoBooth.region,
            address = photoBooth.address,
            reviewCount = totalRatings,
            distance = distance,
            imageUrl = imageUrlList.firstOrNull(),
            isRecommended = isRecommended,
            isFavorite = isFavorite
        )
    }

    /**
     * PhotoBooth Entity를 상세용 Response DTO로 변환
     */
    private fun PhotoBooth.toDetailResponse(
        userPreferredKeywords: List<String>,
        isFavorite: Boolean,
        ratingSummary: RatingSummaryDto?,
        userRating: Int?
    ): PhotoBoothDetailResponse {
        val keywords = getSimpleKeywords(userPreferredKeywords)
        val imageUrlList = parseImageUrls(this.imageUrls)
        val isRecommended = keywords.any { it.isUserPreferred }

        return PhotoBoothDetailResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            keywords = keywords,
            imageUrls = imageUrlList,
            distance = null,
            isRecommended = isRecommended,
            isFavorite = isFavorite,
            averageRating = ratingSummary?.averageRating,
            totalRatings = ratingSummary?.totalRatings?.toInt() ?: 0,
            userRating = userRating
        )
    }

    /**
     * 간소화된 키워드 생성 (추천 서버 연동 전까지 임시)
     */
    private fun getSimpleKeywords(userPreferredKeywords: List<String>): List<KeywordResponse> {
        val defaultKeywords = listOf(
            "자연스러운보정",
            "소품다양",
            "빈티지",
            "화사한톤",
            "친절함"
        )

        return defaultKeywords.map { keyword ->
            KeywordResponse(
                keyword = keyword,
                isUserPreferred = userPreferredKeywords.contains(keyword)
            )
        }
    }

    /**
     * 사용자의 선호 키워드 목록 조회
     */
    private fun getUserPreferredKeywords(userId: String): List<String> {
        return try {
            val user = userRepository.findById(userId).orElse(null)
            parseKeywords(user?.preferredKeywords)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * JSON 키워드 문자열을 List로 파싱
     */
    private fun parseKeywords(keywordsJson: String?): List<String> {
        if (keywordsJson.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val typeRef = object : TypeReference<List<String>>() {}
            objectMapper.readValue(keywordsJson, typeRef)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * JSONB 이미지 URL 문자열을 List로 파싱
     */
    private fun parseImageUrls(imageUrls: String?): List<String> {
        if (imageUrls.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val typeRef = object : TypeReference<List<String>>() {}
            objectMapper.readValue(imageUrls, typeRef)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 두 지점 간 거리 계산 (하버사인 공식)
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadiusM = 6371000.0

        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLng = Math.toRadians(lng2 - lng1)

        val a = sin(deltaLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(deltaLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusM * c
    }
}
