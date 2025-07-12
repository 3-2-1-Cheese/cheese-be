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
    private val objectMapper: ObjectMapper // Spring Boot의 기본 ObjectMapper Bean 주입
) {

    /**
     * 위치 기반 사진관 검색
     * @param lat 사용자 위도 (선택)
     * @param lng 사용자 경도 (선택)
     * @param radius 검색 반경(미터), 기본값 1000m
     * @param region 지역 필터 (강남역, 홍대입구역 등)
     * @param brand 브랜드 필터 (인생네컷, 포토이즘박스 등)
     * @param keyword 통합 검색 키워드 (사진관명/브랜드명/지역)
     * @return 거리순 정렬된 사진관 목록 (위치 없으면 리뷰순)
     */
    fun getPhotoBooths(
        lat: Double?,
        lng: Double?,
        radius: Int?,
        region: String?,
        brand: String?,
        keyword: String?
    ): List<PhotoBoothResponse> {
        val photoBooths = if (lat != null && lng != null) {
            // 위치 기반 검색 (공간 연산 사용)
            val searchRadius = radius ?: 1000
            photoBoothQueryRepository.search(lat, lng, searchRadius, region, brand, keyword)
        } else {
            // 지역/브랜드/키워드만으로 검색 (공간 연산 없음)
            photoBoothQueryRepository.searchWithoutLocation(region, brand, keyword)
        }

        return photoBooths.map {
            if (lat != null && lng != null) {
                it.toResponse(lat, lng)  // 실제 거리 계산
            } else {
                it.toResponseWithoutDistance()  // 거리 0
            }
        }
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
        val imageUrlList = parseImageUrls(this.imageUrls)

        return PhotoBoothResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            rating = this.averageRating?.toDouble(),
            reviewCount = this.reviewCount,
            distance = distance.toInt(),
            imageUrl = imageUrlList.firstOrNull() // 첫 번째 이미지를 대표 이미지로
        )
    }

    /**
     * PhotoBooth Entity를 목록용 Response DTO로 변환 (거리 없음)
     */
    private fun PhotoBooth.toResponseWithoutDistance(): PhotoBoothResponse {
        val imageUrlList = parseImageUrls(this.imageUrls)

        return PhotoBoothResponse(
            id = this.id,
            name = this.name,
            brand = this.brand,
            region = this.region,
            address = this.address,
            rating = this.averageRating?.toDouble(),
            reviewCount = this.reviewCount,
            distance = 0, // 위치 정보 없을 때는 0
            imageUrl = imageUrlList.firstOrNull()
        )
    }
    private fun PhotoBooth.toDetailResponse(): PhotoBoothDetailResponse {
        val operatingHoursMap = parseOperatingHours(this.operatingHours)
        val keywords = extractKeywords(this.analysisData)
        val imageUrlList = parseImageUrls(this.imageUrls)

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
            imageUrls = imageUrlList,
            distance = null
        )
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
            // JSON 파싱 실패 시 빈 리스트 반환
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