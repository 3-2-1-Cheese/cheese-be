package com.hsmile.cheese321.api.photo.controller

import com.hsmile.cheese321.api.photo.dto.*
import com.hsmile.cheese321.api.photo.service.PhotoService
import com.hsmile.cheese321.api.photo.spec.PhotoApi
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import jakarta.validation.Valid

/**
 * 사진 및 앨범 관리
 */
@RestController
class PhotoController(
    private val photoService: PhotoService
) : PhotoApi {

    /**
     * 사진 업로드
     */
    override fun uploadPhoto(
        @AuthenticationPrincipal userId: String,
        photoBoothId: String,
        file: MultipartFile
    ): ResponseEntity<PhotoUploadResponse> {
        val response = photoService.uploadPhoto(userId, photoBoothId, file)
        return ResponseEntity.ok(response)
    }

    /**
     * 내 사진 목록 조회
     */
    override fun getMyPhotos(@AuthenticationPrincipal userId: String): ResponseEntity<List<PhotoResponse>> {
        val photos = photoService.getMyPhotos(userId)
        return ResponseEntity.ok(photos)
    }

    /**
     * 사진 상세 조회
     */
    override fun getPhotoDetail(
        @AuthenticationPrincipal userId: String,
        photoId: String
    ): ResponseEntity<PhotoDetailResponse> {
        val photo = photoService.getPhotoDetail(photoId, userId)
        return ResponseEntity.ok(photo)
    }

    /**
     * 사진 다운로드 URL 생성
     */
    override fun getDownloadUrl(
        @AuthenticationPrincipal userId: String,
        photoId: String
    ): ResponseEntity<PhotoDownloadUrlResponse> {
        val downloadUrl = photoService.generateDownloadUrl(photoId, userId)
        return ResponseEntity.ok(PhotoDownloadUrlResponse(downloadUrl))
    }

    /**
     * 앨범 생성
     */
    override fun createAlbum(
        @AuthenticationPrincipal userId: String,
        request: AlbumCreateRequest
    ): ResponseEntity<AlbumResponse> {
        val album = photoService.createAlbum(userId, request)
        return ResponseEntity.ok(album)
    }

    /**
     * 내 앨범 목록 조회
     */
    override fun getMyAlbums(@AuthenticationPrincipal userId: String): ResponseEntity<List<AlbumResponse>> {
        val albums = photoService.getMyAlbums(userId)
        return ResponseEntity.ok(albums)
    }

    /**
     * 앨범 상세 조회
     */
    override fun getAlbumDetail(
        @AuthenticationPrincipal userId: String,
        albumId: String
    ): ResponseEntity<AlbumDetailResponse> {
        val album = photoService.getAlbumDetail(albumId, userId)
        return ResponseEntity.ok(album)
    }

    /**
     * 앨범에 사진 추가
     */
    override fun addPhotosToAlbum(
        @AuthenticationPrincipal userId: String,
        albumId: String,
        request: AlbumAddPhotosRequest
    ): ResponseEntity<AlbumDetailResponse> {
        val album = photoService.addPhotosToAlbum(albumId, request, userId)
        return ResponseEntity.ok(album)
    }

    /**
     * 앨범 수정
     */
    override fun updateAlbum(
        @AuthenticationPrincipal userId: String,
        albumId: String,
        request: AlbumUpdateRequest
    ): ResponseEntity<AlbumResponse> {
        val album = photoService.updateAlbum(userId, albumId, request)
        return ResponseEntity.ok(album)
    }

    /**
     * 앨범 삭제
     */
    override fun deleteAlbum(
        @AuthenticationPrincipal userId: String,
        albumId: String
    ): ResponseEntity<AlbumDeleteResponse> {
        val response = photoService.deleteAlbum(userId, albumId)
        return ResponseEntity.ok(response)
    }

    /**
     * 앨범에서 사진 제거
     */
    override fun removePhotosFromAlbum(
        @AuthenticationPrincipal userId: String,
        albumId: String,
        request: AlbumRemovePhotosRequest
    ): ResponseEntity<Map<String, String>> {
        val response = photoService.removePhotosFromAlbum(userId, albumId, request)
        return ResponseEntity.ok(response)
    }
}