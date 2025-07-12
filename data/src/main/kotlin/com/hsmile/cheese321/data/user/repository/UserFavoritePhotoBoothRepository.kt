package com.hsmile.cheese321.data.user.repository

import com.hsmile.cheese321.data.user.entity.UserFavoritePhotoBooth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 사용자 관심 사진관 데이터 접근 Repository
 */
@Repository
interface UserFavoritePhotoBoothRepository : JpaRepository<UserFavoritePhotoBooth, String> {

    /**
     * 사용자별 관심 사진관 조회 (최신순)
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<UserFavoritePhotoBooth>

    /**
     * 특정 사용자가 특정 사진관을 찜했는지 확인
     */
    fun existsByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): Boolean

    /**
     * 특정 사용자의 특정 사진관 찜 정보 조회
     */
    fun findByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): UserFavoritePhotoBooth?

    /**
     * 특정 사용자의 특정 사진관 찜 정보 삭제
     */
    fun deleteByUserIdAndPhotoBoothId(userId: String, photoBoothId: String)

    /**
     * 여러 사진관에 대한 사용자의 찜 상태를 한번에 조회 (N+1 방지)
     */
    @Query("SELECT uf.photoBoothId FROM UserFavoritePhotoBooth uf WHERE uf.userId = :userId AND uf.photoBoothId IN :photoBoothIds")
    fun findFavoritePhotoBoothIds(userId: String, photoBoothIds: List<String>): List<String>

    /**
     * 사용자별 찜한 사진관 개수 조회
     */
    fun countByUserId(userId: String): Long
}