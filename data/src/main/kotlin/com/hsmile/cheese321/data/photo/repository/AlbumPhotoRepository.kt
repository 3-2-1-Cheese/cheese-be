package com.hsmile.cheese321.data.photo.repository

import com.hsmile.cheese321.data.photo.entity.AlbumPhoto
import com.hsmile.cheese321.data.photo.repository.dto.AlbumPhotoCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
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
     * 특정 사진이 포함된 앨범들 조회
     */
    fun findByPhotoIdOrderByCreatedAtDesc(photoId: String): List<AlbumPhoto>

    /**
     * 여러 앨범의 사진 개수를 한번에 조회 (N+1 방지)
     */
    @Query("""
        SELECT new map(ap.albumId as albumId, COUNT(ap.photoId) as count) 
        FROM AlbumPhoto ap 
        WHERE ap.albumId IN :albumIds 
        GROUP BY ap.albumId
    """)
    fun countByAlbumIds(albumIds: List<String>): List<AlbumPhotoCount>

    /**
     * 특정 앨범에 이미 있는 사진 ID들을 조회
     */
    @Query("SELECT ap.photoId FROM AlbumPhoto ap WHERE ap.albumId = :albumId AND ap.photoId IN :photoIds")
    fun findExistingPhotoIdsInAlbum(albumId: String, photoIds: List<String>): List<String>

    /**
     * 앨범에서 특정 사진들 제거
     */
    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.albumId = :albumId AND ap.photoId IN :photoIds")
    fun removePhotosFromAlbum(albumId: String, photoIds: List<String>): Int

    /**
     * 앨범에서 모든 사진 제거
     */
    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.albumId = :albumId")
    fun removeAllPhotosFromAlbum(albumId: String): Int

    /**
     * 특정 사진을 모든 앨범에서 제거 (사진 삭제 시 사용)
     */
    @Modifying
    @Query("DELETE FROM AlbumPhoto ap WHERE ap.photoId = :photoId")
    fun removePhotoFromAllAlbums(photoId: String): Int

    /**
     * 사용자의 앨범에 속한 사진들만 조회 (권한 체크용)
     */
    @Query("""
        SELECT ap FROM AlbumPhoto ap 
        JOIN Album a ON ap.albumId = a.id 
        WHERE a.userId = :userId AND ap.photoId IN :photoIds
    """)
    fun findByUserIdAndPhotoIds(userId: String, photoIds: List<String>): List<AlbumPhoto>
}