package com.hsmile.cheese321.data.photo.repository

import com.hsmile.cheese321.data.photo.entity.Album
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 앨범 데이터 접근 Repository
 */
@Repository
interface AlbumRepository : JpaRepository<Album, String> {

    /**
     * 사용자별 앨범 조회 (생성일시 역순)
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Album>

    /**
     * 사용자의 기본 앨범 조회
     */
    fun findByUserIdAndIsDefaultTrue(userId: String): Album?

    /**
     * 사용자별 앨범 개수 조회
     */
    fun countByUserId(userId: String): Long

    /**
     * 사용자의 특정 앨범 조회 (권한 확인용)
     */
    @Query("SELECT a FROM Album a WHERE a.id = :albumId AND a.userId = :userId")
    fun findByIdAndUserId(albumId: String, userId: String): Album?
}