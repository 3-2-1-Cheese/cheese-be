package com.hsmile.cheese321.api.photo.service

import com.hsmile.cheese321.api.photo.dto.*
import com.hsmile.cheese321.api.user.service.VisitHistoryService
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
    private val photoBoothRepository: PhotoBoothRepository,
    private val visitHistoryService: VisitHistoryService
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

        // 방문 기록 추가
        // 방문 기록 실패해도 사진 업로드는 성공으로 처리
        try {
            visitHistoryService.recordVisit(userId, photoBoothId)
        } catch (e: Exception) {
            // 방문 기록 실패는 로그만 남기고 계속 진행
            // TODO: 실제 운영 시 로깅 시스템 사용
            println("방문 기록 호출 실패 - userId: $userId, photoBoothId: $photoBoothId")
        }

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

        if (photos.isEmpty()) {
            return emptyList()
        }

        // 한번에 모든 사진관 정보 조회
        val photoBoothIds = photos.map { it.photoBoothId }.distinct()
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id }

        return photos.map { photo ->
            val photoBoothName = photoBooths[photo.photoBoothId]?.name ?: "알 수 없는 사진관"

            PhotoResponse(
                id = photo.id,
                originalFilename = photo.originalFilename,
                photoBoothName = photoBoothName,
                createdAt = photo.createdAt.toString(),
                downloadUrl = "https://temp-download-url.com/${photo.filePath}" // TODO: 실제 URL 생성
            )
        }
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

        val photoBoothName = photoBoothRepository.findById(photo.photoBoothId)
            .map { it.name }
            .orElse("알 수 없는 사진관")

        return PhotoDetailResponse(
            id = photo.id,
            originalFilename = photo.originalFilename,
            photoBoothName = photoBoothName,
            fileSize = photo.fileSize,
            contentType = photo.contentType,
            createdAt = photo.createdAt.toString(),
            downloadUrl = "https://temp-download-url.com/${photo.filePath}" // TODO: 실제 URL 생성
        )
    }

    /**
     * 개별 사진 삭제 (영구 삭제)
     */
    fun deletePhoto(userId: String, photoId: String): Map<String, String> {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("사진을 찾을 수 없습니다: $photoId") }

        if (photo.userId != userId) {
            throw IllegalArgumentException("사진 접근 권한이 없습니다")
        }

        // 모든 앨범에서 사진 제거
        albumPhotoRepository.removePhotoFromAllAlbums(photoId)

        // 사진 삭제
        photoRepository.delete(photo)

        // TODO: S3에서 파일 삭제

        return mapOf("message" to "사진이 성공적으로 삭제되었습니다")
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

    /**
     * 사진 일괄 삭제 (영구 삭제)
     */
    fun batchDeletePhotos(userId: String, request: PhotoBatchDeleteRequest): PhotoBatchDeleteResponse {
        // 사용자 소유 사진들만 필터링
        val allPhotos = photoRepository.findAllById(request.photoIds)
        val ownedPhotos = allPhotos.filter { it.userId == userId }
        val ownedPhotoIds = ownedPhotos.map { it.id }

        var totalRemovedFromAlbums = 0

        if (ownedPhotoIds.isNotEmpty()) {
            // 모든 앨범에서 한번에 제거
            totalRemovedFromAlbums = albumPhotoRepository.removePhotosFromAllAlbums(ownedPhotoIds)

            // 한번에 삭제
            photoRepository.deleteAllById(ownedPhotoIds)

            // TODO: S3에서 여러 파일 한번에 삭제
        }

        return PhotoBatchDeleteResponse(
            deletedCount = ownedPhotos.size,
            skippedCount = request.photoIds.size - ownedPhotos.size,
            removedFromAlbumsCount = totalRemovedFromAlbums,
            message = "${ownedPhotos.size}개의 사진이 성공적으로 삭제되었습니다."
        )
    }

    /**
     * 여러 사진을 특정 앨범으로 이동
     */
    fun movePhotosToAlbum(userId: String, request: PhotoMoveRequest): PhotoMoveResponse {
        // 앨범 소유권 확인
        val album = albumRepository.findById(request.targetAlbumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: ${request.targetAlbumId}") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범에 대한 접근 권한이 없습니다")
        }

        // 사용자 소유 사진들만 필터링
        val allPhotos = photoRepository.findAllById(request.photoIds)
        val ownedPhotos = allPhotos.filter { it.userId == userId }
        val ownedPhotoIds = ownedPhotos.map { it.id }

        if (ownedPhotoIds.isEmpty()) {
            return PhotoMoveResponse(
                movedCount = 0,
                skippedCount = request.photoIds.size,
                alreadyInAlbumCount = 0,
                message = "이동할 수 있는 사진이 없습니다."
            )
        }

        // 이미 앨범에 있는 사진들 확인
        val existingPhotoIds = albumPhotoRepository.findPhotoIdsByAlbumId(request.targetAlbumId)
            .intersect(ownedPhotoIds.toSet())

        // 새로 추가할 사진들
        val newPhotoIds = ownedPhotoIds - existingPhotoIds

        // 한번에 저장
        val newAlbumPhotos = newPhotoIds.map { photoId ->
            AlbumPhoto(albumId = request.targetAlbumId, photoId = photoId)
        }

        if (newAlbumPhotos.isNotEmpty()) {
            albumPhotoRepository.saveAll(newAlbumPhotos)
        }

        return PhotoMoveResponse(
            movedCount = newAlbumPhotos.size,
            skippedCount = request.photoIds.size - ownedPhotos.size,
            alreadyInAlbumCount = existingPhotoIds.size,
            message = "${newAlbumPhotos.size}개의 사진이 앨범으로 이동되었습니다."
        )
    }

    /**
     * 앨범에서 여러 사진 일괄 제거
     */
    fun batchRemovePhotosFromAlbum(userId: String, albumId: String, request: AlbumPhotoBatchRemoveRequest): AlbumPhotoRemoveResponse {
        // 앨범 소유권 확인
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != userId) {
            throw IllegalArgumentException("앨범 접근 권한이 없습니다")
        }

        if (album.isDefault) {
            throw IllegalArgumentException("기본 앨범에서는 사진을 제거할 수 없습니다")
        }

        // 실제로 앨범에 있는 사진들만 필터링
        val existingPhotoIds = albumPhotoRepository.findPhotoIdsByAlbumId(albumId)
        val photosToRemove = request.photoIds.intersect(existingPhotoIds.toSet())

        // [성능 최적화] 여러 사진을 한번에 제거
        val removedCount = albumPhotoRepository.removePhotosFromAlbum(albumId, photosToRemove.toList())

        return AlbumPhotoRemoveResponse(
            removedCount = removedCount,
            notFoundCount = request.photoIds.size - photosToRemove.size,
            message = "${removedCount}개의 사진이 앨범에서 제거되었습니다."
        )
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

        return AlbumResponse(
            id = album.id,
            name = album.name,
            description = album.description,
            photoCount = 0L, // 새로 생성된 앨범은 사진 0개
            coverImageUrl = album.coverImageUrl,
            createdAt = album.createdAt.toString(),
            isDefault = album.isDefault
        )
    }

    /**
     * 내 앨범 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyAlbums(userId: String): List<AlbumResponse> {
        val albums = albumRepository.findByUserIdOrderByCreatedAtDesc(userId)

        if (albums.isEmpty()) {
            return emptyList()
        }

        // 한번에 모든 앨범의 사진 개수 조회 (N+1 방지)
        val albumIds = albums.map { it.id }
        val photoCounts = albumPhotoRepository.countByAlbumIds(albumIds)
            .associateBy { it.albumId }

        return albums.map { album ->
            AlbumResponse(
                id = album.id,
                name = album.name,
                description = album.description,
                photoCount = photoCounts[album.id]?.count ?: 0L,
                coverImageUrl = album.coverImageUrl,
                createdAt = album.createdAt.toString(),
                isDefault = album.isDefault
            )
        }
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

        if (albumPhotos.isEmpty()) {
            return AlbumDetailResponse(
                id = album.id,
                name = album.name,
                description = album.description,
                photoCount = 0,
                photos = emptyList(),
                coverImageUrl = album.coverImageUrl,
                createdAt = album.createdAt.toString(),
                isDefault = album.isDefault
            )
        }

        val photoIds = albumPhotos.map { it.photoId }
        val photos = photoRepository.findAllById(photoIds).associateBy { it.id }

        // 사진관 정보 한번에 조회 (N+1 방지)
        val photoBoothIds = photos.values.map { it.photoBoothId }.distinct()
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id }

        // 순서대로 정렬된 사진 목록 생성
        val orderedPhotos = albumPhotos.mapNotNull { albumPhoto ->
            val photo = photos[albumPhoto.photoId]
            photo?.let {
                val photoBoothName = photoBooths[it.photoBoothId]?.name ?: "알 수 없는 사진관"

                PhotoResponse(
                    id = it.id,
                    originalFilename = it.originalFilename,
                    photoBoothName = photoBoothName,
                    createdAt = it.createdAt.toString(),
                    downloadUrl = "https://temp-download-url.com/${it.filePath}"
                )
            }
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

        // 사진 개수 조회
        val photoCount = albumPhotoRepository.countByAlbumId(albumId)

        return AlbumResponse(
            id = album.id,
            name = album.name,
            description = album.description,
            photoCount = photoCount,
            coverImageUrl = album.coverImageUrl,
            createdAt = album.createdAt.toString(),
            isDefault = album.isDefault
        )
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
}