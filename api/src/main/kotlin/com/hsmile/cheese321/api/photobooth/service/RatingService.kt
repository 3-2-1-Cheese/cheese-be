package com.hsmile.cheese321.api.photobooth.service

import com.hsmile.cheese321.api.photobooth.dto.*
import com.hsmile.cheese321.data.photobooth.entity.PhotoBoothRating
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRatingRepository
import com.hsmile.cheese321.data.photobooth.exception.PhotoBoothNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사진관 별점 관리 서비스
 */
@Service
@Transactional
class RatingService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoBoothRatingRepository: PhotoBoothRatingRepository
) {

    /**
     * 별점 등록 또는 수정
     */
    fun upsertRating(userId: String, photoBoothId: String, ratingValue: Int): RatingResponse {
        // 사진관 존재 확인
        val photoBooth = photoBoothRepository.findById(photoBoothId)
            .orElseThrow { PhotoBoothNotFoundException("PhotoBooth not found with id: $photoBoothId") }

        // 기존 별점 확인
        val existingRating = photoBoothRatingRepository.findByUserIdAndPhotoBoothId(userId, photoBoothId)

        val message = if (existingRating != null) {
            // 기존 별점 수정
            val updatedRating = existingRating.updateRating(ratingValue)
            photoBoothRatingRepository.save(updatedRating)
            "${photoBooth.name}의 별점을 ${ratingValue}점으로 수정했습니다"
        } else {
            // 새로운 별점 등록
            val newRating = PhotoBoothRating(
                userId = userId,
                photoBoothId = photoBoothId,
                rating = ratingValue
            )
            photoBoothRatingRepository.save(newRating)
            "${photoBooth.name}에 ${ratingValue}점을 등록했습니다"
        }

        // 업데이트된 평점 통계 조회 (DTO 방식)
        val ratingSummary = photoBoothRatingRepository.findRatingSummary(photoBoothId)

        return RatingResponse(
            photoBoothId = photoBoothId,
            rating = ratingValue,
            message = message,
            newAverageRating = ratingSummary?.averageRating,
            totalRatings = ratingSummary?.totalRatings?.toInt() ?: 0
        )
    }

    /**
     * 별점 삭제
     */
    fun deleteRating(userId: String, photoBoothId: String): RatingDeleteResponse {
        // 사진관 존재 확인
        val photoBooth = photoBoothRepository.findById(photoBoothId)
            .orElseThrow { PhotoBoothNotFoundException("PhotoBooth not found with id: $photoBoothId") }

        // 기존 별점 확인
        val existingRating = photoBoothRatingRepository.findByUserIdAndPhotoBoothId(userId, photoBoothId)
            ?: throw IllegalArgumentException("등록된 별점이 없습니다")

        // 별점 삭제
        photoBoothRatingRepository.deleteByUserIdAndPhotoBoothId(userId, photoBoothId)

        // 업데이트된 평점 통계 조회
        val ratingSummary = photoBoothRatingRepository.findRatingSummary(photoBoothId)

        return RatingDeleteResponse(
            photoBoothId = photoBoothId,
            message = "${photoBooth.name}의 별점을 삭제했습니다",
            newAverageRating = ratingSummary?.averageRating,
            totalRatings = ratingSummary?.totalRatings?.toInt() ?: 0
        )
    }
}