package com.hsmile.cheese321.api.qr.service

import com.hsmile.cheese321.api.qr.dto.*
import com.hsmile.cheese321.api.photo.service.PhotoService
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * QR 스캔 및 사진 저장 서비스
 */
@Service
@Transactional
class QrService(
    private val photoBoothRepository: PhotoBoothRepository,
    private val photoService: PhotoService
) {

    /**
     * QR 코드 스캔 및 분석
     */
    fun scanQr(userId: String, request: QrScanRequest): QrScanResponse {
        val qrData = request.qrData.trim()

        return when {
            // 인생네컷 QR 패턴
            qrData.contains("life4cut.net") -> {
                parseLife4CutQr(qrData)
            }

            // 포토이즘박스 QR 패턴
            qrData.contains("photoismbox.com") -> {
                parsePhotoismBoxQr(qrData)
            }

            // 포토그레이 QR 패턴
            qrData.contains("photogray.com") -> {
                parsePhotoGrayQr(qrData)
            }

            // 기타 브랜드 또는 일반 URL
            qrData.startsWith("http") -> {
                parseGenericPhotoQr(qrData)
            }

            // 사진관 ID 직접 입력 (테스트용)
            else -> {
                parsePhotoBoothId(qrData)
            }
        }
    }

    /**
     * QR로 가져온 사진들을 앱에 저장
     */
    fun savePhotosFromQr(userId: String, request: SavePhotosFromQrRequest): SavePhotosFromQrResponse {
        // TODO: 실제 구현에서는 외부 URL에서 사진 다운로드 필요
        val photoIds = mutableListOf<String>()

        // 1. 사진관 정보 처리
        val photoBoothId = request.photoBoothId ?: "unknown-booth"

        // 2. 앨범 생성
        val albumCreateRequest = com.hsmile.cheese321.api.photo.dto.AlbumCreateRequest(
            name = request.albumName,
            description = "QR 스캔으로 가져온 사진들"
        )
        val album = photoService.createAlbum(userId, albumCreateRequest)

        // 3. 사진 다운로드 및 저장 (현재는 더미 구현)
        request.photoUrls.forEach { photoUrl ->
            // TODO: 실제로는 photoUrl에서 이미지를 다운로드해서 저장
            // val photoId = downloadAndSavePhoto(userId, photoBoothId, photoUrl)
            // photoIds.add(photoId)

            // 현재는 더미 데이터
            photoIds.add("dummy-photo-${System.currentTimeMillis()}")
        }

        // 4. 방문 기록 추가 (나중에 구현)
        val visitRecordAdded = false // TODO: 방문 기록 시스템 구현 후 활성화

        return SavePhotosFromQrResponse(
            albumId = album.id,
            albumName = album.name,
            savedPhotoCount = photoIds.size,
            photoIds = photoIds,
            visitRecordAdded = visitRecordAdded,
            message = "${request.albumName} 앨범에 ${photoIds.size}개 사진이 저장되었습니다"
        )
    }

    // ===== QR 파싱 메서드들 =====

    /**
     * 인생네컷 QR 파싱
     */
    private fun parseLife4CutQr(qrData: String): QrScanResponse {
        // 예시 QR: https://life4cut.net/album/abc123
        return try {
            val albumId = extractAlbumId(qrData, "life4cut.net/album/")
            val photoUrls = fetchLife4CutPhotos(albumId) // TODO: 실제 구현 필요

            QrScanResponse(
                scanType = QrScanType.PHOTO_URLS,
                photoUrls = photoUrls,
                photoBoothInfo = PhotoBoothInfo(
                    id = null,
                    name = "인생네컷",
                    brand = "인생네컷",
                    address = null
                ),
                suggestedAlbumName = generateAlbumName("인생네컷"),
                canSave = photoUrls.isNotEmpty(),
                message = if (photoUrls.isNotEmpty()) {
                    "${photoUrls.size}개 사진을 찾았습니다"
                } else {
                    "사진을 찾을 수 없습니다"
                }
            )
        } catch (e: Exception) {
            createUnsupportedResponse("인생네컷 QR 분석 실패: ${e.message}")
        }
    }

    /**
     * 포토이즘박스 QR 파싱
     */
    private fun parsePhotoismBoxQr(qrData: String): QrScanResponse {
        // 예시 QR: https://photoismbox.com/share/xyz789
        return try {
            val shareId = extractAlbumId(qrData, "photoismbox.com/share/")
            val photoUrls = fetchPhotoismBoxPhotos(shareId) // TODO: 실제 구현 필요

            QrScanResponse(
                scanType = QrScanType.PHOTO_URLS,
                photoUrls = photoUrls,
                photoBoothInfo = PhotoBoothInfo(
                    id = null,
                    name = "포토이즘박스",
                    brand = "포토이즘박스",
                    address = null
                ),
                suggestedAlbumName = generateAlbumName("포토이즘박스"),
                canSave = photoUrls.isNotEmpty(),
                message = if (photoUrls.isNotEmpty()) {
                    "${photoUrls.size}개 사진을 찾았습니다"
                } else {
                    "사진을 찾을 수 없습니다"
                }
            )
        } catch (e: Exception) {
            createUnsupportedResponse("포토이즘박스 QR 분석 실패: ${e.message}")
        }
    }

    /**
     * 포토그레이 QR 파싱
     */
    private fun parsePhotoGrayQr(qrData: String): QrScanResponse {
        return try {
            val shareId = extractAlbumId(qrData, "photogray.com/gallery/")
            val photoUrls = fetchPhotoGrayPhotos(shareId) // TODO: 실제 구현 필요

            QrScanResponse(
                scanType = QrScanType.PHOTO_URLS,
                photoUrls = photoUrls,
                photoBoothInfo = PhotoBoothInfo(
                    id = null,
                    name = "포토그레이",
                    brand = "포토그레이",
                    address = null
                ),
                suggestedAlbumName = generateAlbumName("포토그레이"),
                canSave = photoUrls.isNotEmpty(),
                message = if (photoUrls.isNotEmpty()) {
                    "${photoUrls.size}개 사진을 찾았습니다"
                } else {
                    "사진을 찾을 수 없습니다"
                }
            )
        } catch (e: Exception) {
            createUnsupportedResponse("포토그레이 QR 분석 실패: ${e.message}")
        }
    }

    /**
     * 일반 URL QR 파싱
     */
    private fun parseGenericPhotoQr(qrData: String): QrScanResponse {
        return createUnsupportedResponse("지원하지 않는 QR 형식입니다")
    }

    /**
     * 사진관 ID QR 파싱 (테스트용)
     */
    private fun parsePhotoBoothId(qrData: String): QrScanResponse {
        val photoBooth = photoBoothRepository.findById(qrData).orElse(null)

        return if (photoBooth != null) {
            QrScanResponse(
                scanType = QrScanType.PHOTOBOOTH_INFO,
                photoUrls = null,
                photoBoothInfo = PhotoBoothInfo(
                    id = photoBooth.id,
                    name = photoBooth.name,
                    brand = photoBooth.brand,
                    address = photoBooth.address
                ),
                suggestedAlbumName = generateAlbumName(photoBooth.name),
                canSave = false,
                message = "${photoBooth.name} 정보를 찾았습니다"
            )
        } else {
            createUnsupportedResponse("사진관을 찾을 수 없습니다: $qrData")
        }
    }

    // ===== 유틸리티 메서드들 =====

    private fun extractAlbumId(qrData: String, pattern: String): String {
        val index = qrData.indexOf(pattern)
        if (index == -1) {
            throw IllegalArgumentException("유효하지 않은 QR 형식")
        }

        return qrData.substring(index + pattern.length).split("/", "?", "#").first()
    }

    private fun generateAlbumName(brandName: String): String {
        val now = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        return "${now.format(dateFormatter)} $brandName"
    }

    private fun createUnsupportedResponse(message: String): QrScanResponse {
        return QrScanResponse(
            scanType = QrScanType.UNSUPPORTED,
            photoUrls = null,
            photoBoothInfo = null,
            suggestedAlbumName = null,
            canSave = false,
            message = message
        )
    }

    // TODO: 실제 사진 가져오기 메서드들 (현재는 더미)
    private fun fetchLife4CutPhotos(albumId: String): List<String> {
        // 현재는 더미 데이터
        return listOf(
            "https://temp-life4cut.com/photo1.jpg",
            "https://temp-life4cut.com/photo2.jpg",
            "https://temp-life4cut.com/photo3.jpg",
            "https://temp-life4cut.com/photo4.jpg"
        )
    }

    private fun fetchPhotoismBoxPhotos(shareId: String): List<String> {
        // 현재는 더미 데이터
        return listOf(
            "https://temp-photoismbox.com/photo1.jpg",
            "https://temp-photoismbox.com/photo2.jpg"
        )
    }

    private fun fetchPhotoGrayPhotos(shareId: String): List<String> {
        // 현재는 더미 데이터
        return listOf(
            "https://temp-photogray.com/photo1.jpg",
            "https://temp-photogray.com/photo2.jpg"
        )
    }
}