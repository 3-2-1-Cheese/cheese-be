// api/src/main/kotlin/com/hsmile/cheese321/api/photobooth/service/PhotoBoothService.kt

package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.dto.KeywordResponse
import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothQueryRepository
import com.hsmile.cheese321.data.photobooth.exception.PhotoBoothNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import kotlin.math.*

/**
 * 사진관 비즈니스 로직 처리 서비스
 * PostGIS 기반 위치 검색, 거리 계산, 필터링 기능 제공
 */
@Service
@Transactional(readOnly = true)
class PhotoBoothService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothQueryRepository: PhotoBoothQueryRepository,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {

    /**
     * 위치 기반 사진관 검색
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radius 검색 반경(미터), 기본값 1000m
     * @param region 지역 필터 (강남역, 홍대입구역 등)
     * @param brand 브랜드 필터 (인생네컷, 포토이즘박스 등)
     * @return 거리순 정렬된 사진관 목록
     */
    fun getPhotoBooths(
        lat: Double,
        lng: Double,
        radius: Int?,
        region: String?,
        brand: String?
    ): List<PhotoBoothResponse> {
        val searchRadius = radius ?: 1000

        val photoBooths = photoBoothQueryRepository.search(lat, lng, searchRadius, region, brand)
        return photoBooths.map { it.toResponse(lat, lng) }
    }

    /**
     * 사진관 상세 정보 조회
     * @param id 사진관 고유 ID
     * @return 사진관 상세 정보
     * @throws PhotoBoothNotFoundException 사진관을 찾을 수 없는 경우
     */
    fun getPhotoBoothDetail(id: String): PhotoBoothDetailResponse {
        val photoBooth = photoBoothRepository.findByIdOrNull(id)
            ?: throw PhotoBoothNotFoundException("PhotoBooth not found with id: $id")

        return photoBooth.toDetailResponse()
    }

    /**
     * PhotoBooth Entity를 목록용 Response DTO로 변환
     * 사용자 위치로부터의 거리 계산 포함
     */
    private fun PhotoBooth.toResponse(userLat: Double, userLng: Double): PhotoBoothResponse {
        val distance = calculateDistance(userLat, userLng, this.location.y, this.location.x)

        return PhotoBoothResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            rating = this.averageRating?.toDouble(),
            reviewCount = this.reviewCount,
            distance = distance.toInt(),
            imageUrl = this.imageUrls?.firstOrNull()
        )
    }

    /**
     * PhotoBooth Entity를 상세용 Response DTO로 변환
     * 운영시간 파싱, 키워드 추출 등 상세 정보 처리
     */
    private fun PhotoBooth.toDetailResponse(): PhotoBoothDetailResponse {
        val operatingHoursMap = parseOperatingHours(this.operatingHours)
        val keywords = extractKeywords(this.analysisData)

        return PhotoBoothDetailResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            phoneNumber = this.phoneNumber,
            operatingHours = operatingHoursMap,
            boothCount = this.boothCount ?: 0,
            capacity = this.capacity ?: 0,
            rating = this.averageRating?.toDouble(),
            reviewCount = this.reviewCount,
            positiveRatio = this.positiveRatio?.toDouble(),
            keywords = keywords,
            imageUrls = this.imageUrls?.toList() ?: emptyList(),
            distance = null
        )
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
     * JSONB 운영시간을 Map으로 파싱
     * @param operatingHours JSONB 문자열
     * @return 요일별 운영시간 Map
     */
    private fun parseOperatingHours(operatingHours: String?): Map<String, String> {
        if (operatingHours.isNullOrBlank()) {
            return getDefaultOperatingHours()
        }

        return try {
            val typeRef = object : TypeReference<Map<String, String>>() {}
            objectMapper.readValue(operatingHours, typeRef)
        } catch (e: Exception) {
            getDefaultOperatingHours()
        }
    }

    /**
     * AI 분석 데이터에서 키워드 추출
     * TODO: AI팀 데이터 구조 확정 후 구현
     */
    private fun extractKeywords(analysisData: String?): List<KeywordResponse> {
        if (analysisData.isNullOrBlank()) {
            return emptyList()
        }

        // TODO: AI 분석 데이터 파싱 로직 구현
        return listOf(
            KeywordResponse(
                keyword = "자연스러운보정",
                type = "사진스타일",
                isUserPreferred = false,
                relevanceScore = 0.85
            )
        )
    }

    /**
     * 기본 운영시간 반환 (10:00-22:00)
     */
    private fun getDefaultOperatingHours(): Map<String, String> {
        val defaultHours = "10:00-22:00"
        return mapOf(
            "monday" to defaultHours,
            "tuesday" to defaultHours,
            "wednesday" to defaultHours,
            "thursday" to defaultHours,
            "friday" to defaultHours,
            "saturday" to defaultHours,
            "sunday" to defaultHours
        )
    }
}