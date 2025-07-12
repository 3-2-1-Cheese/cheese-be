// api/src/main/kotlin/com/hsmile/cheese321/api/photobooth/service/PhotoBoothService.kt

package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.dto.KeywordResponse
import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothQueryRepository
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
 * PostGIS 기반 위치 검색, 거리 계산, 필터링 기능 제공
 * 개인화 기능 포함 (키워드 강조, 찜하기)
 */
@Service
@Transactional(readOnly = true)
class PhotoBoothService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothQueryRepository: PhotoBoothQueryRepository,
    private val userRepository: UserRepository,
    private val userFavoritePhotoBoothRepository: UserFavoritePhotoBoothRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * 위치 기반 사진관 검색 (개인화 정보 포함)
     * @param userId 사용자 ID (개인화를 위해)
     * @param lat 사용자 위도 (선택)
     * @param lng 사용자 경도 (선택)
     * @param radius 검색 반경(미터), 기본값 1000m
     * @param region 지역 필터 (강남역, 홍대입구역 등)
     * @param brand 브랜드 필터 (인생네컷, 포토이즘박스 등)
     * @param keyword 통합 검색 키워드 (사진관명/브랜드명/지역)
     * @return 거리순 정렬된 사진관 목록 (개인화 정보 포함)
     */
    fun getPhotoBooths(
        userId: String,
        lat: Double?,
        lng: Double?,
        radius: Int?,
        region: String?,
        brand: String?,
        keyword: String?
    ): List<PhotoBoothResponse> {
        val photoBooths = if (lat != null && lng != null) {
            val searchRadius = radius ?: 1000
            photoBoothQueryRepository.search(lat, lng, searchRadius, region, brand, keyword)
        } else {
            photoBoothQueryRepository.searchWithoutLocation(region, brand, keyword)
        }

        // 개인화 정보를 위해 사용자 선호 키워드와 찜 목록을 미리 조회
        val userPreferredKeywords = getUserPreferredKeywords(userId)
        val favoriteBoothIds = userFavoritePhotoBoothRepository.findFavoritePhotoBoothIds(
            userId,
            photoBooths.map { it.id }
        ).toSet()

        return photoBooths.map { booth ->
            booth.toResponse(
                userLat = lat,
                userLng = lng,
                userPreferredKeywords = userPreferredKeywords,
                isFavorite = favoriteBoothIds.contains(booth.id)
            )
        }
    }

    /**
     * 사진관 상세 정보 조회 (개인화 정보 포함)
     * @param id 사진관 고유 ID
     * @param userId 요청한 사용자 ID (키워드 강조를 위해)
     * @return 사진관 상세 정보
     * @throws PhotoBoothNotFoundException 사진관을 찾을 수 없는 경우
     */
    fun getPhotoBoothDetail(id: String, userId: String): PhotoBoothDetailResponse {
        val photoBooth = photoBoothRepository.findByIdOrNull(id)
            ?: throw PhotoBoothNotFoundException("PhotoBooth not found with id: $id")

        val userPreferredKeywords = getUserPreferredKeywords(userId)
        val isFavorite = userFavoritePhotoBoothRepository.existsByUserIdAndPhotoBoothId(userId, id)

        return photoBooth.toDetailResponse(userPreferredKeywords, isFavorite)
    }

    /**
     * PhotoBooth Entity를 목록용 Response DTO로 변환 (개인화 정보 포함)
     */
    private fun PhotoBooth.toResponse(
        userLat: Double?,
        userLng: Double?,
        userPreferredKeywords: List<String>,
        isFavorite: Boolean
    ): PhotoBoothResponse {
        val distance = if (userLat != null && userLng != null) {
            calculateDistance(userLat, userLng, this.location.y, this.location.x).toInt()
        } else 0

        val imageUrlList = parseImageUrls(this.imageUrls)

        // 이 사진관이 사용자의 선호 키워드를 포함하는지 여부
        val keywords = extractKeywords(this.analysisData, userPreferredKeywords)
        val isRecommended = keywords.any { it.isUserPreferred }

        return PhotoBoothResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            reviewCount = this.reviewCount,
            distance = distance,
            imageUrl = imageUrlList.firstOrNull(),
            isRecommended = isRecommended,
            isFavorite = isFavorite
        )
    }

    /**
     * PhotoBooth Entity를 상세용 Response DTO로 변환 (개인화 정보 포함)
     */
    private fun PhotoBooth.toDetailResponse(
        userPreferredKeywords: List<String>,
        isFavorite: Boolean
    ): PhotoBoothDetailResponse {
        val keywords = extractKeywords(this.analysisData, userPreferredKeywords)
        val imageUrlList = parseImageUrls(this.imageUrls)

        val isRecommended = keywords.any { it.isUserPreferred }

        return PhotoBoothDetailResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            reviewCount = this.reviewCount,
            keywords = keywords,
            imageUrls = imageUrlList,
            distance = null,
            isRecommended = isRecommended,
            isFavorite = isFavorite
        )
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
     * @param imageUrls JSONB 문자열 (예: "[\"url1\", \"url2\"]")
     * @return 이미지 URL 리스트
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
     * @return 거리(미터)
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

    /**
     * AI 분석 데이터에서 키워드 추출 및 사용자 선호 키워드와 비교
     * TODO: AI팀 데이터 구조 확정 후 구현
     */
    private fun extractKeywords(analysisData: String?, userKeywords: List<String>): List<KeywordResponse> {
        if (analysisData.isNullOrBlank()) {
            return getDefaultKeywords(userKeywords)
        }

        // TODO: AI 분석 데이터 파싱 로직 구현
        // 현재는 더미 데이터로 테스트
        val photoBoothKeywords = listOf(
            "자연스러운보정" to "사진스타일",
            "하이앵글" to "촬영스타일",
            "소품다양" to "소품",
            "빈티지" to "분위기",
            "화사한톤" to "사진스타일"
        )

        return photoBoothKeywords.map { (keyword, type) ->
            KeywordResponse(
                keyword = keyword,
                type = type,
                isUserPreferred = userKeywords.contains(keyword),
                relevanceScore = if (userKeywords.contains(keyword)) 0.95 else 0.75
            )
        }
    }

    /**
     * 기본 키워드 반환 (분석 데이터가 없을 때)
     */
    private fun getDefaultKeywords(userKeywords: List<String>): List<KeywordResponse> {
        val defaultKeywords = listOf(
            "자연스러운보정" to "사진스타일",
            "소품다양" to "소품"
        )

        return defaultKeywords.map { (keyword, type) ->
            KeywordResponse(
                keyword = keyword,
                type = type,
                isUserPreferred = userKeywords.contains(keyword),
                relevanceScore = 0.70
            )
        }
    }
}