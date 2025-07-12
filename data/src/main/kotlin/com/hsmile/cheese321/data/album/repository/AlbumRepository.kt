package com.hsmile.cheese321.data.album.repository

import com.hsmile.cheese321.data.album.entity.Album
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 앨범 데이터 접근 Repository
 */
@Repository
interface AlbumRepository : JpaRepository<Album, String> {

    /**
     * 사용자별 앨범 조회 (최신순)
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<Album>
}