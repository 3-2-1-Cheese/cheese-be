package com.hsmile.cheese321.data.photo.repository

import com.hsmile.cheese321.data.photo.entity.Photo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사진 데이터 접근 Repository
 */
@Repository
interface PhotoRepository : JpaRepository<Photo, String> {

    /**
     * 사용자별 사진 조회 (최신순)
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Photo>

    /**
     * 특정 사진관에서 찍은 사진들 조회
     */
    fun findByPhotoBoothIdOrderByCreatedAtDesc(photoBoothId: String): List<Photo>

    /**
     * 사용자별 특정 사진관 사진들 조회
     */
    fun findByUserIdAndPhotoBoothIdOrderByCreatedAtDesc(userId: String, photoBoothId: String): List<Photo>
}