package com.hsmile.cheese321.api.user.service

import com.hsmile.cheese321.api.user.dto.FavoritePhotoBoothsResponse
import com.hsmile.cheese321.api.user.dto.FavoritePhotoBoothInfo
import com.hsmile.cheese321.api.user.dto.FavoriteToggleResponse
import com.hsmile.cheese321.data.user.entity.UserFavoritePhotoBooth
import com.hsmile.cheese321.data.user.repository.UserFavoritePhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * 사진관 찜하기 관리 서비스
 */
@Service
@Transactional
class FavoriteService(
    private val userFavoritePhotoBoothRepository: UserFavoritePhotoBoothRepository,
    private val photoBoothRepository: PhotoBoothRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * 사진관 찜하기/취소 토글
     */
    fun toggleFavorite(userId: String, photoBoothId: String): FavoriteToggleResponse {
        // 사진관 존재 확인
        val photoBooth = photoBoothRepository.findById(photoBoothId)
            .orElseThrow { IllegalArgumentException("사진관을 찾을 수 없습니다: $photoBoothId") }

        // 기존 찜하기 상태 확인
        val existingFavorite = userFavoritePhotoBoothRepository.findByUserIdAndPhotoBoothId(userId, photoBoothId)

        return if (existingFavorite != null) {
            // 이미 찜한 상태 → 찜하기 취소
            userFavoritePhotoBoothRepository.deleteByUserIdAndPhotoBoothId(userId, photoBoothId)
            FavoriteToggleResponse(
                photoBoothId = photoBoothId,
                isFavorite = false,
                message = "${photoBooth.name}을(를) 관심 목록에서 제거했습니다"
            )
        } else {
            // 찜하지 않은 상태 → 찜하기 추가
            val newFavorite = UserFavoritePhotoBooth(
                userId = userId,
                photoBoothId = photoBoothId,
                createdAt = LocalDateTime.now()
            )
            userFavoritePhotoBoothRepository.save(newFavorite)
            FavoriteToggleResponse(
                photoBoothId = photoBoothId,
                isFavorite = true,
                message = "${photoBooth.name}을(를) 관심 목록에 추가했습니다"
            )
        }
    }

    /**
     * 내가 찜한 사진관 목록 조회
     */
    @Transactional(readOnly = true)
    fun getFavoritePhotoBooths(userId: String): FavoritePhotoBoothsResponse {
        val favorites = userFavoritePhotoBoothRepository.findByUserIdOrderByCreatedAtDesc(userId)

        if (favorites.isEmpty()) {
            return FavoritePhotoBoothsResponse(
                photoBooths = emptyList(),
                totalCount = 0
            )
        }

        // 사진관 정보를 한번에 조회 (N+1 방지)
        val photoBoothIds = favorites.map { it.photoBoothId }
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id }

        val favoriteInfos = favorites.mapNotNull { favorite ->
            val photoBooth = photoBooths[favorite.photoBoothId] ?: return@mapNotNull null
            val imageUrls = parseImageUrls(photoBooth.imageUrls)

            FavoritePhotoBoothInfo(
                id = photoBooth.id,
                name = photoBooth.name,
                brand = photoBooth.brand,
                region = photoBooth.region,
                address = photoBooth.address,
                imageUrl = imageUrls.firstOrNull(),
                favoritedAt = favorite.createdAt.toString()
            )
        }

        return FavoritePhotoBoothsResponse(
            photoBooths = favoriteInfos,
            totalCount = favoriteInfos.size
        )
    }

    /**
     * 특정 사진관의 찜 상태 확인
     */
    @Transactional(readOnly = true)
    fun isFavorite(userId: String, photoBoothId: String): Boolean {
        return userFavoritePhotoBoothRepository.existsByUserIdAndPhotoBoothId(userId, photoBoothId)
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
}