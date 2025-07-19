package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.common.client.ai.AIServiceClient
import com.hsmile.cheese321.api.common.client.ai.UserProfileForAI
import com.hsmile.cheese321.api.common.client.ai.UserLocation
import com.hsmile.cheese321.api.common.client.ai.AIRecommendationResponse
import com.hsmile.cheese321.api.user.service.UserService
import com.hsmile.cheese321.api.user.service.VisitHistoryService
import com.hsmile.cheese321.api.user.service.FavoriteService
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRatingRepository
import com.hsmile.cheese321.data.user.repository.UserFavoritePhotoBoothRepository
import com.hsmile.cheese321.data.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사진관 추천 서비스
 */
@Service
@Transactional(readOnly = true)
class PhotoBoothRecommendationService(
    private val photoBoothService: PhotoBoothService,
    private val userService: UserService,
    private val visitHistoryService: VisitHistoryService,
    private val favoriteService: FavoriteService,
    private val aiServiceClient: AIServiceClient,
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothRatingRepository: PhotoBoothRatingRepository, // 추가
    private val userFavoritePhotoBoothRepository: UserFavoritePhotoBoothRepository,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * 개인화 추천 사진관 목록
     */
    suspend fun getRecommendedPhotoBooths(
        userId: String,
        userLat: Double?,
        userLng: Double?,
        limit: Int = 20
    ): List<PhotoBoothResponse> {
        return try {
            // 1. 기존 서비스들로 사용자 프로필 생성
            val userProfile = createUserProfileForAI(userId, userLat, userLng)

            // 2. 추천 서비스 호출
            val aiResponse = aiServiceClient.getRecommendations(userProfile)

            if (aiResponse != null && aiResponse.recommendations.isNotEmpty()) {
                // 3. 추천 결과 기반으로 사진관 상세 정보 조합
                getPhotoBoothsByAIRecommendation(userId, aiResponse, userLat, userLng, limit)
            } else {
                // 4. 추천 서비스 실패 시 폴백
                getFallbackRecommendations(userId, userLat, userLng, limit)
            }
        } catch (e: Exception) {
            // 예외 발생 시 폴백
            getFallbackRecommendations(userId, userLat, userLng, limit)
        }
    }

    /**
     * 기존 서비스들로 사용자 프로필 생성
     */
    private fun createUserProfileForAI(userId: String, userLat: Double?, userLng: Double?): UserProfileForAI {
        // 1. 사용자 키워드
        val preferredKeywords = try {
            userService.getPreferredKeywords(userId).keywords
        } catch (e: Exception) {
            emptyList()
        }

        // 2. 최근 방문 기록
        val recentVisits = try {
            visitHistoryService.getRecentVisits(userId).visits
                .take(10)
                .map { it.photoBoothId }
        } catch (e: Exception) {
            emptyList()
        }

        // 3. 찜한 사진관
        val favoritePhotoBooths = try {
            favoriteService.getFavoritePhotoBooths(userId).photoBooths
                .map { it.id }
        } catch (e: Exception) {
            emptyList()
        }

        // 4. 사용자 위치
        val location = if (userLat != null && userLng != null) {
            UserLocation(userLat, userLng)
        } else null

        return UserProfileForAI(
            userId = userId,
            preferredKeywords = preferredKeywords,
            location = location,
            recentVisits = recentVisits,
            favoritePhotoBooths = favoritePhotoBooths
        )
    }

    /**
     * 추천 결과를 기반으로 사진관 상세 정보 조합 (N+1 최적화)
     */
    private fun getPhotoBoothsByAIRecommendation(
        userId: String,
        aiResponse: AIRecommendationResponse,
        userLat: Double?,
        userLng: Double?,
        limit: Int
    ): List<PhotoBoothResponse> {
        // 1. 추천 사진관 ID들 추출
        val recommendedIds = aiResponse.recommendations
            .take(limit)
            .map { it.photoBoothId }

        if (recommendedIds.isEmpty()) return emptyList()

        // 2. 추천된 사진관들만 DB에서 조회
        val photoBooths = photoBoothRepository.findAllById(recommendedIds)
            .associateBy { it.id }

        // 3. 사용자 개인화 정보 및 평점 정보 일괄 조회
        val userPreferredKeywords = getUserPreferredKeywords(userId)
        val favoritePhotoBoothIds = userFavoritePhotoBoothRepository
            .findFavoritePhotoBoothIds(userId, recommendedIds)
            .toSet()

        // 평점 정보 일괄 조회
        val ratingSummaries = photoBoothRatingRepository.findRatingSummaries(recommendedIds)
            .associateBy { it.photoBoothId }

        // 4. 추천 순서대로 정렬하여 응답 생성 (PhotoBoothService 재사용)
        return recommendedIds.mapNotNull { photoBoothId ->
            val photoBooth = photoBooths[photoBoothId] ?: return@mapNotNull null
            val ratingSummary = ratingSummaries[photoBoothId]

            // PhotoBoothService의 변환 로직 재사용
            photoBoothService.convertToResponse(
                photoBooth = photoBooth,
                userLat = userLat,
                userLng = userLng,
                userPreferredKeywords = userPreferredKeywords,
                isFavorite = favoritePhotoBoothIds.contains(photoBoothId),
                totalRatings = ratingSummary?.totalRatings?.toInt() ?: 0
            ).copy(isRecommended = true) // AI 추천이므로 true로 오버라이드
        }
    }

    /**
     * 추천 서비스 실패 시 폴백
     */
    private fun getFallbackRecommendations(
        userId: String,
        userLat: Double?,
        userLng: Double?,
        limit: Int
    ): List<PhotoBoothResponse> {
        return photoBoothService.getPhotoBooths(
            userId = userId,
            lat = userLat,
            lng = userLng,
            radius = if (userLat != null && userLng != null) 5000 else null,
            region = null,
            brand = null,
            keyword = null
        ).take(limit)
    }

    /**
     * 사용자 선호 키워드 조회
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
     * JSON 키워드 문자열 파싱
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
}