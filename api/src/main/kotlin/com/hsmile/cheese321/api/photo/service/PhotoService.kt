package com.hsmile.cheese321.api.photo.service

import com.hsmile.cheese321.api.photo.dto.*
import com.hsmile.cheese321.data.photo.entity.Photo
import com.hsmile.cheese321.data.photo.repository.PhotoRepository
import com.hsmile.cheese321.data.album.entity.Album
import com.hsmile.cheese321.data.album.entity.AlbumPhoto
import com.hsmile.cheese321.data.album.repository.AlbumRepository
import com.hsmile.cheese321.data.album.repository.AlbumPhotoRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
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
    // TODO: S3Service 나중에 추가
) {

    /**
     * 사진 업로드
     */
    fun uploadPhoto(userId: String, photoBoothId: String, file: MultipartFile): PhotoUploadResponse {
        // TODO: S3에 파일 업로드 처리
        val filePath = "temp/${UUID.randomUUID()}_${file.originalFilename}"

        val photo = Photo(
            id = UUID.randomUUID().toString(),
            userId = userId,
            photoBoothId = photoBoothId,
            filePath = filePath,
            originalFilename = file.originalFilename ?: "unknown.jpg",
            fileSize = file.size,
            contentType = file.contentType ?: "image/jpeg",
            createdAt = LocalDateTime.now()
        )

        val savedPhoto = photoRepository.save(photo)

        // TODO: 실제 다운로드 URL 생성 (S3 presigned URL)
        val downloadUrl = "/api/v1/photos/${savedPhoto.id}/download"

        return PhotoUploadResponse(
            photoId = savedPhoto.id,
            downloadUrl = downloadUrl
        )
    }

    /**
     * 내 사진 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyPhotos(userId: String): List<PhotoResponse> {
        val photos = photoRepository.findByUserIdOrderByCreatedAtDesc(userId)
        if (photos.isEmpty()) return emptyList()

        // 필요한 photoBoothId를 모두 추출
        val photoBoothIds = photos.map { it.photoBoothId }.distinct()

        // ID 목록으로 사진관 정보를 한번에 조회 (IN 쿼리)
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id } // 검색하기 쉽도록 Map으로 변환

        return photos.map { photo ->
            val photoBooth = photoBooths[photo.photoBoothId]
            PhotoResponse(
                id = photo.id,
                originalFilename = photo.originalFilename,
                photoBoothName = photoBooth?.name ?: "알 수 없는 사진관",
                createdAt = photo.createdAt.toString(),
                downloadUrl = "/api/v1/photos/${photo.id}/download" // TODO: 실제 URL
            )
        }
    }

    /**
     * 사진 상세 조회
     */
    @Transactional(readOnly = true)
    fun getPhotoDetail(photoId: String, requestUserId: String): PhotoDetailResponse {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("사진을 찾을 수 없습니다: $photoId") }

        // 권한 체크: 본인 사진만 조회 가능
        if (photo.userId != requestUserId) {
            throw IllegalAccessException("해당 사진에 접근할 권한이 없습니다")
        }

        val photoBooth = photoBoothRepository.findById(photo.photoBoothId).orElse(null)

        return PhotoDetailResponse(
            id = photo.id,
            originalFilename = photo.originalFilename,
            photoBoothName = photoBooth?.name ?: "알 수 없는 사진관",
            fileSize = photo.fileSize,
            contentType = photo.contentType,
            createdAt = photo.createdAt.toString(),
            downloadUrl = "/api/v1/photos/${photo.id}/download" // TODO: 실제 URL
        )
    }

    /**
     * 앨범 생성
     */
    fun createAlbum(userId: String, request: AlbumCreateRequest): AlbumResponse {
        val album = Album(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = request.name,
            description = request.description,
            createdAt = LocalDateTime.now()
        )

        val savedAlbum = albumRepository.save(album)

        return AlbumResponse(
            id = savedAlbum.id,
            name = savedAlbum.name,
            description = savedAlbum.description,
            photoCount = 0,
            createdAt = savedAlbum.createdAt.toString()
        )
    }

    /**
     * 내 앨범 목록 조회
     */
    @Transactional(readOnly = true)
    fun getMyAlbums(userId: String): List<AlbumResponse> {
        val albums = albumRepository.findByUserIdOrderByCreatedAtDesc(userId)
        if (albums.isEmpty()) return emptyList()

        val albumIds = albums.map { it.id }
        // 앨범 ID별 사진 개수를 DTO로 한번에 조회
        val photoCounts = albumPhotoRepository.countPhotosInAlbums(albumIds)
            .associateBy { it.albumId }

        return albums.map { album ->
            val photoCount = photoCounts[album.id]?.photoCount ?: 0L
            AlbumResponse(
                id = album.id,
                name = album.name,
                description = album.description,
                photoCount = photoCount,
                createdAt = album.createdAt.toString()
            )
        }
    }

    /**
     * 앨범 상세 조회
     */
    @Transactional(readOnly = true)
    fun getAlbumDetail(albumId: String, requestUserId: String): AlbumDetailResponse {
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        // 권한 체크: 본인 앨범만 조회 가능
        if (album.userId != requestUserId) {
            throw IllegalAccessException("해당 앨범에 접근할 권한이 없습니다")
        }

        val albumPhotos = albumPhotoRepository.findByAlbumIdOrderByCreatedAtDesc(albumId)
        val photoIds = albumPhotos.map { it.photoId }
        val photos = photoRepository.findAllById(photoIds)

        val photoBoothIds = photos.map { it.photoBoothId }.distinct()
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id }

        val photoResponses = photos.map { photo ->
            val photoBooth = photoBooths[photo.photoBoothId]
            PhotoResponse(
                id = photo.id,
                originalFilename = photo.originalFilename,
                photoBoothName = photoBooth?.name ?: "알 수 없는 사진관",
                createdAt = photo.createdAt.toString(),
                downloadUrl = "/api/v1/photos/${photo.id}/download"
            )
        }

        return AlbumDetailResponse(
            id = album.id,
            name = album.name,
            description = album.description,
            photoCount = photos.size,
            photos = photoResponses,
            createdAt = album.createdAt.toString()
        )
    }

    /**
     * 앨범에 사진 추가
     */
    fun addPhotosToAlbum(albumId: String, request: AlbumAddPhotosRequest, requestUserId: String): AlbumDetailResponse {
        // 앨범 존재 확인 및 권한 체크
        val album = albumRepository.findById(albumId)
            .orElseThrow { IllegalArgumentException("앨범을 찾을 수 없습니다: $albumId") }

        if (album.userId != requestUserId) {
            throw IllegalAccessException("해당 앨범에 접근할 권한이 없습니다")
        }

        // 사진들 존재 확인 및 권한 체크
        val photos = photoRepository.findAllById(request.photoIds)
        if (photos.size != request.photoIds.size) {
            throw IllegalArgumentException("일부 사진을 찾을 수 없습니다")
        }

        // 본인 사진만 앨범에 추가 가능
        val invalidPhotos = photos.filter { it.userId != requestUserId }
        if (invalidPhotos.isNotEmpty()) {
            throw IllegalAccessException("다른 사용자의 사진을 앨범에 추가할 수 없습니다")
        }

        // 이미 앨범에 있는 사진 ID들을 한번에 조회
        val existingPhotoIds = albumPhotoRepository.findExistingPhotoIdsInAlbum(albumId, request.photoIds)
            .toSet()

        // 새로 추가할 사진들만 필터링
        val newPhotosToAdd = request.photoIds
            .filter { it !in existingPhotoIds }
            .map { photoId ->
                AlbumPhoto(
                    id = UUID.randomUUID().toString(),
                    albumId = albumId,
                    photoId = photoId,
                    createdAt = LocalDateTime.now()
                )
            }

        // 새로 추가할 사진들만 한번에 저장
        if (newPhotosToAdd.isNotEmpty()) {
            albumPhotoRepository.saveAll(newPhotosToAdd)
        }

        // 업데이트된 앨범 상세 정보 반환
        return getAlbumDetail(albumId, requestUserId)
    }

    /**
     * 다운로드 URL 생성 (권한 체크 포함)
     */
    @Transactional(readOnly = true)
    fun generateDownloadUrl(photoId: String, requestUserId: String): String {
        val photo = photoRepository.findById(photoId)
            .orElseThrow { IllegalArgumentException("사진을 찾을 수 없습니다: $photoId") }

        // 권한 체크: 본인 사진만 다운로드 가능
        if (photo.userId != requestUserId) {
            throw IllegalAccessException("해당 사진에 접근할 권한이 없습니다")
        }

        // TODO: S3 presigned URL 생성
        return "/api/v1/photos/${photo.id}/download"
    }
}