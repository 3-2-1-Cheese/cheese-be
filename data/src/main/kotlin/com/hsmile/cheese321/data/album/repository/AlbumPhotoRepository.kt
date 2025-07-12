package com.hsmile.cheese321.data.album.repository

import com.hsmile.cheese321.data.album.entity.AlbumPhoto
import com.hsmile.cheese321.data.album.repository.dto.AlbumPhotoCountDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 앨범-사진 연결 데이터 접근 Repository
 */
@Repository
interface AlbumPhotoRepository : JpaRepository<AlbumPhoto, String> {

    /**
     * 앨범별 사진 조회 (추가된 순서대로)
     */
    fun findByAlbumIdOrderByCreatedAtAsc(albumId: String): List<AlbumPhoto>

    /**
     * 앨범별 사진 조회 (최신순) - 앨범 상세에서 사용
     */
    fun findByAlbumIdOrderByCreatedAtDesc(albumId: String): List<AlbumPhoto>

    /**
     * 앨범에 특정 사진 존재 여부 확인
     */
    fun existsByAlbumIdAndPhotoId(albumId: String, photoId: String): Boolean

    /**
     * 앨범 내 사진 개수 조회
     */
    fun countByAlbumId(albumId: String): Long

    /**
     * 여러 앨범의 사진 개수를 한번에 조회
     */
    @Query("""
        SELECT new com.hsmile.cheese321.data.album.repository.dto.AlbumPhotoCountDto(ap.albumId, COUNT(ap))
        FROM AlbumPhoto ap
        WHERE ap.albumId IN :albumIds
        GROUP BY ap.albumId
    """)
    fun countPhotosInAlbums(albumIds: List<String>): List<AlbumPhotoCountDto>

    /**
     * 특정 앨범에 이미 있는 사진 ID들을 조회
     */
    @Query("SELECT ap.photoId FROM AlbumPhoto ap WHERE ap.albumId = :albumId AND ap.photoId IN :photoIds")
    fun findExistingPhotoIdsInAlbum(albumId: String, photoIds: List<String>): List<String>

    // TODO: 나중에 구현할 기능들
    // - 사진별 포함된 앨범 조회 (findByPhotoId)
    // - 순서 기반 정렬 (findByAlbumIdOrderBySortOrder)
}