package com.hsmile.cheese321.api.photo.service

import com.hsmile.cheese321.api.photo.dto.*
import com.hsmile.cheese321.data.photo.entity.Photo
import com.hsmile.cheese321.data.photo.entity.Album
import com.hsmile.cheese321.data.photo.entity.AlbumPhoto
import com.hsmile.cheese321.data.photo.repository.PhotoRepository
import com.hsmile.cheese321.data.photo.repository.AlbumRepository
import com.hsmile.cheese321.data.photo.repository.AlbumPhotoRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * 사진 및 앨범 관리 서비스
 */
@Service
@Transactional
class PhotoService(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val albumPhotoRepository: AlbumPhotoRepository,
    private val photoBoothRepository: PhotoBoothRepository
) {

    // ===== 사진 관련 메서드들 =====

    /**
     * 사진 업로드
     */
    fun uploadPhoto(userId: String, photoBoothId: String, file: MultipartFile): PhotoUploadResponse {
        // TODO: S3 업로드 로직 구현 필요
        val photoId = UUID.randomUUID().toString()
        val filePath = "temp/path/${photoId}_${file.originalFilename}"

        val photo = Photo(
            id = photoId,
            userId = userId,
            photoBoothId = photoBoothId,
            filePath = filePath,
            originalFilename = file.originalFilename ?: "unknown",
            fileSize = file.size,
            contentType = file.contentType ?: "image/jpeg"
        )

        photoRepository.save(photo)

        // 기본 앨범에 자동 추가
        val defaultAlbum = getOrCreateDefaultAlbum(userId)
        addPhotoToAlbum(defaultAlbum.id, photoId)

        return PhotoUploadResponse(
            photoId = photoId,
            downloadUrl = generateDownloadUrl(photoId, userId)
        )
    }

    /**
     * 내 사진 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyPhotos(userId: String): List<PhotoResponse> {
        val photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userId)
        return photos.map { it.toPhotoResponse() }
    }

    /**
     * 사진 상세 조회
     */
    @Transactional(readOnly = true)
    fun getPhotoDetail(photoId: String, userId: String): PhotoDetailResponse {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("사진을 찾을 수 없습니다: $photoId") }

        if (photo.userId != userId) {
            throw IllegalArgumentException("사진 접근 권한이 없습니다")
        }

        return photo.toPhotoDetailResponse()
    }

    /**
     * 다운로드 URL 생성
     */
    @Transactional(readOnly = true)
    fun generateDownloadUrl(photoId: String, userId: String): String {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("사진을 찾을 수 없습니다: $photoId") }

        if (photo.userId != userId) {
            throw IllegalArgumentException("사진 접근 권한이 없습니다")
        }

        // TODO: S3 Pre-signed URL 생성 로직 구현 필요
        return "https://temp-download-url.com/${photo.filePath}"
    }

    // ===== 앨범 관련 메서드들 =====

    /**
     * 앨범 생성
     */
    fun createAlbum(userId: String, request: AlbumCreateRequest): AlbumResponse {
        val album = Album(
            userId = userId,
            name = request.name,
            description = request.description
        )

        albumRepository.save(album)
        return album.toAlbumResponse()
    }

    /**
     * 내 앨범 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyAlbums(userId: String): List<AlbumResponse> {
        val albums = albumRepository.findByUserIdOrderByCreatedAtDesc(userId)
        return albums.map { it.toAlbumResponse() }
    }

    /**
     * 앨범 상세 조회
     */
    @Transactional(readOnly = true)
    fun getAlbumDetail(albumId: String, userId: String): AlbumDetailResponse {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 접근 권한이 없습니다")
        }

        // AlbumPhoto를 통해 사진들 조회
        val albumPhotos = albumPhotoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId)
        val photoIds = albumPhotos.map { it.photoId }
        val photos = if (photoIds.isNotEmpty()) {
            photoRepository.findAllById(photoIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        // 순서대로 정렬된 사진 목록 생성
        val orderedPhotos = albumPhotos.mapNotNull { albumPhoto ->
            photos[albumPhoto.photoId]?.toPhotoResponse()
        }

        return AlbumDetailResponse(
            id = album.id,
            name = album.name,
            description = album.description,
            photoCount = orderedPhotos.size,
            photos = orderedPhotos,
            coverImageUrl = album.coverImageUrl,
            createdAt = album.createdAt.toString(),
            isDefault = album.isDefault
        )
    }

    /**
     * 앨범 수정
     */
    fun updateAlbum(userId: String, albumId: String, request: AlbumUpdateRequest): AlbumResponse {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 수정 권한이 없습니다")
        }

        if (album.isDefault) {
            throw IllegalArgumentException("기본 앨범은 수정할 수 없습니다")
        }

        album.updateInfo(request.name, request.description)
        albumRepository.save(album)

        return album.toAlbumResponse()
    }

    /**
     * 앨범 삭제 (사진은 전체 앨범에 남김)
     */
    fun deleteAlbum(userId: String, albumId: String): AlbumDeleteResponse {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 삭제 권한이 없습니다")
        }

        if (album.isDefault) {
            throw IllegalArgumentException("기본 앨범은 삭제할 수 없습니다")
        }

        // 앨범에 포함된 사진 개수 확인
        val photoCount = albumPhotoRepository.countByAlbumId(albumId)

        // 앨범-사진 연결만 삭제 (사진 자체는 보존)
        val removedPhotoCount = albumPhotoRepository.removeAllPhotosFromAlbum(albumId)

        // 앨범 삭제
        albumRepository.delete(album)

        return AlbumDeleteResponse(
            albumId = albumId,
            message = "${album.name} 앨범이 삭제되었습니다. ${removedPhotoCount}개 사진은 '전체 사진'에서 확인할 수 있습니다",
            deletedPhotoCount = 0 // 실제로는 사진을 삭제하지 않음
        )
    }

    /**
     * 앨범에 사진 추가
     */
    fun addPhotosToAlbum(albumId: String, request: AlbumAddPhotosRequest, userId: String): AlbumDetailResponse {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 접근 권한이 없습니다")
        }

        // 사용자 소유 사진인지 확인
        val photos = photoRepository.findAllById(request.photoIds)
        photos.forEach { photo ->
            if (photo.userId != userId) {
                throw IllegalArgumentException("사진 접근 권한이 없습니다: ${photo.id}")
            }
        }

        // 이미 앨범에 있는 사진들 확인
        val existingPhotoIds = albumPhotoRepository.findExistingPhotoIdsInAlbum(albumId, request.photoIds)
        val newPhotoIds = request.photoIds - existingPhotoIds.toSet()

        // 새로운 사진들만 앨범에 추가
        newPhotoIds.forEach { photoId ->
            addPhotoToAlbum(albumId, photoId)
        }

        return getAlbumDetail(albumId, userId)
    }

    /**
     * 앨범에서 사진 제거
     */
    fun removePhotosFromAlbum(userId: String, albumId: String, request: AlbumRemovePhotosRequest): Map<String, String> {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 접근 권한이 없습니다")
        }

        if (album.isDefault) {
            throw IllegalArgumentException("기본 앨범에서는 사진을 제거할 수 없습니다")
        }

        // 앨범에서 사진들 제거
        val removedCount = albumPhotoRepository.removePhotosFromAlbum(albumId, request.photoIds)

        return mapOf(
            "message" to "${removedCount}개 사진이 앨범에서 제거되었습니다",
            "albumId" to albumId
        )
    }

    // ===== 유틸리티 메서드들 =====

    /**
     * 기본 앨범 조회 또는 생성
     */
    private fun getOrCreateDefaultAlbum(userId: String): Album {
        return albumRepository.findByUserIdAndIsDefaultTrue(userId)
            ?: run {
                val defaultAlbum = Album.createDefaultAlbum(userId)
                albumRepository.save(defaultAlbum)
            }
    }

    /**
     * 앨범에 사진 추가 (내부 유틸리티)
     */
    private fun addPhotoToAlbum(albumId: String, photoId: String) {
        if (!albumPhotoRepository.existsByAlbumIdAndPhotoId(albumId, photoId)) {
            val albumPhoto = AlbumPhoto(
                albumId = albumId,
                photoId = photoId
            )
            albumPhotoRepository.save(albumPhoto)
        }
    }

    /**
     * Photo Entity를 PhotoResponse DTO로 변환
     */
    private fun Photo.toPhotoResponse(): PhotoResponse {
        val photoBoothName = try {
            photoBoothRepository.findById(this.photoBoothId)
                .map { it.name }
                .orElse("알 수 없는 사진관")
        } catch (e: Exception) {
            "알 수 없는 사진관"
        }

        return PhotoResponse(
            id = this.id,
            originalFilename = this.originalFilename,
            photoBoothName = photoBoothName,
            createdAt = this.createdAt.toString(),
            downloadUrl = generateDownloadUrl(this.id, this.userId)
        )
    }

    /**
     * Photo Entity를 PhotoDetailResponse DTO로 변환
     */
    private fun Photo.toPhotoDetailResponse(): PhotoDetailResponse {
        val photoBoothName = try {
            photoBoothRepository.findById(this.photoBoothId)
                .map { it.name }
                .orElse("알 수 없는 사진관")
        } catch (e: Exception) {
            "알 수 없는 사진관"
        }

        return PhotoDetailResponse(
            id = this.id,
            originalFilename = this.originalFilename,
            photoBoothName = photoBoothName,
            fileSize = this.fileSize,
            contentType = this.contentType,
            createdAt = this.createdAt.toString(),
            downloadUrl = generateDownloadUrl(this.id, this.userId)
        )
    }

    /**
     * Album Entity를 AlbumResponse DTO로 변환
     */
    private fun Album.toAlbumResponse(): AlbumResponse {
        val photoCount = albumPhotoRepository.countByAlbumId(this.id)

        return AlbumResponse(
            id = this.id,
            name = this.name,
            description = this.description,
            photoCount = photoCount,
            coverImageUrl = this.coverImageUrl,
            createdAt = this.createdAt.toString(),
            isDefault = this.isDefault
        )
    }
}