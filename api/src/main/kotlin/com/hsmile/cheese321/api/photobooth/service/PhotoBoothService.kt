// api/src/main/kotlin/com/hsmile/cheese321/api/photobooth/service/PhotoBoothService.kt

package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothQueryRepository
import com.hsmile.cheese321.data.photobooth.repository.dto.PhotoBoothSearchCriteria
import com.hsmile.cheese321.data.photobooth.exception.PhotoBoothNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PhotoBoothService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothQueryRepository: PhotoBoothQueryRepository
) {

    fun getPhotoBooths(
        lat: Double,
        lng: Double,
        radius: Int?,
        region: String?,
        brand: String?
    ): List<PhotoBoothResponse> {
        val criteria = PhotoBoothSearchCriteria(
            lat = lat,
            lng = lng,
            radius = radius,
            region = region,
            brand = brand
        )

        val photoBooths = photoBoothQueryRepository.search(criteria)
        return photoBooths.map { it.toResponse(lat, lng) }
    }

    fun getPhotoBoothDetail(id: String): PhotoBoothDetailResponse {
        val photoBooth = photoBoothRepository.findByIdOrNull(id)
            ?: throw PhotoBoothNotFoundException("PhotoBooth not found with id: $id")

        return photoBooth.toDetailResponse(null, null)
    }

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
            imageUrl = this.imageUrls?.firstOrNull() // 대표 이미지만
        )
    }

    private fun PhotoBooth.toDetailResponse(userLat: Double?, userLng: Double?): PhotoBoothDetailResponse {
        val distance = if (userLat != null && userLng != null) {
            calculateDistance(userLat, userLng, this.location.y, this.location.x).toInt()
        } else null

        // JSONB String을 Map으로 변환 (임시로 빈 Map 반환)
        val operatingHoursMap = parseOperatingHours(this.operatingHours)

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
            keywords = emptyList(), // TODO: 키워드 테이블 연동 후 구현
            imageUrls = this.imageUrls?.toList() ?: emptyList(),
            distance = distance
        )
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        // 간단한 직선 거리 계산 (정확한 계산은 PostGIS에서)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371000 * c // 지구 반지름(미터)
    }

    private fun parseOperatingHours(operatingHours: String?): Map<String, String> {
        // TODO: JSONB String을 Map으로 파싱
        return emptyMap()
    }
}