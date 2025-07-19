package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBoothRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 사진관 별점 Repository
 */
@Repository
interface PhotoBoothRatingRepository : JpaRepository<PhotoBoothRating, String> {

    /**
     * 특정 사용자의 특정 사진관 별점 조회
     */
    fun findByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): PhotoBoothRating?

    /**
     * 특정 사용자의 특정 사진관 별점 존재 여부
     */
    fun existsByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): Boolean

    /**
     * 특정 사용자의 특정 사진관 별점 삭제
     */
    fun deleteByUserIdAndPhotoBoothId(userId: String, photoBoothId: String)

    /**
     * 특정 사진관의 평균 별점 및 총 개수 조회
     */
    @Query("""
        SELECT AVG(CAST(r.rating AS double)), COUNT(r.rating)
        FROM PhotoBoothRating r 
        WHERE r.photoBoothId = :photoBoothId
    """)
    fun findAverageRatingAndCount(photoBoothId: String): Array<Any?>

    /**
     * 여러 사진관의 평균 별점 및 총 개수 일괄 조회 (N+1 방지)
     */
    @Query("""
        SELECT new com.hsmile.cheese321.data.photobooth.repository.dto.RatingSummaryDto(
            r.photoBoothId, AVG(CAST(r.rating AS double)), COUNT(r.rating)
        )
        FROM PhotoBoothRating r 
        WHERE r.photoBoothId IN :photoBoothIds
        GROUP BY r.photoBoothId
    """)
    fun findRatingSummaries(photoBoothIds: List<String>): List<com.hsmile.cheese321.data.photobooth.repository.dto.RatingSummaryDto>

    /**
     * 특정 사진관의 평점 요약 조회 (단일)
     */
    @Query("""
        SELECT new com.hsmile.cheese321.data.photobooth.repository.dto.RatingSummaryDto(
            :photoBoothId, AVG(CAST(r.rating AS double)), COUNT(r.rating)
        )
        FROM PhotoBoothRating r 
        WHERE r.photoBoothId = :photoBoothId
    """)
    fun findRatingSummary(photoBoothId: String): com.hsmile.cheese321.data.photobooth.repository.dto.RatingSummaryDto?

    /**
     * 특정 사용자들의 특정 사진관들에 대한 별점 일괄 조회
     */
    @Query("""
        SELECT r.photoBoothId, r.rating
        FROM PhotoBoothRating r 
        WHERE r.userId = :userId 
        AND r.photoBoothId IN :photoBoothIds
    """)
    fun findUserRatingsForPhotoBooths(userId: String, photoBoothIds: List<String>): List<Array<Any>>

    /**
     * 특정 사진관의 총 별점 개수
     */
    fun countByPhotoBoothId(photoBoothId: String): Long

    /**
     * 특정 사용자의 총 별점 개수
     */
    fun countByUserId(userId: String): Long
}