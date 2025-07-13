package com.hsmile.cheese321.api.qr.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// ===== QR 스캔 관련 DTO =====

@Schema(description = "QR 스캔 요청")
data class QrScanRequest(
    @Schema(description = "QR 코드 데이터", example = "https://life4cut.net/album/abc123")
    @field:NotBlank(message = "QR 데이터는 필수입니다")
    val qrData: String
)

@Schema(description = "QR 스캔 응답")
data class QrScanResponse(
    @Schema(description = "QR 스캔 결과 타입", example = "PHOTO_URLS")
    val scanType: QrScanType,

    @Schema(description = "사진 URL 목록 (인생네컷 등에서 가져온 사진들)")
    val photoUrls: List<String>?,

    @Schema(description = "사진관 정보")
    val photoBoothInfo: PhotoBoothInfo?,

    @Schema(description = "자동 생성된 앨범명")
    val suggestedAlbumName: String?,

    @Schema(description = "저장 가능 여부")
    val canSave: Boolean,

    @Schema(description = "메시지")
    val message: String
)

@Schema(description = "QR 스캔 결과 타입")
enum class QrScanType {
    @Schema(description = "사진 URL들을 포함한 QR (인생네컷 등)")
    PHOTO_URLS,

    @Schema(description = "사진관 정보만 포함한 QR")
    PHOTOBOOTH_INFO,

    @Schema(description = "지원하지 않는 QR 형식")
    UNSUPPORTED
}

@Schema(description = "사진관 정보")
data class PhotoBoothInfo(
    @Schema(description = "사진관 ID")
    val id: String?,

    @Schema(description = "사진관 이름")
    val name: String,

    @Schema(description = "브랜드명")
    val brand: String,

    @Schema(description = "주소")
    val address: String?
)

// ===== QR로 사진 저장 관련 DTO =====

@Schema(description = "QR에서 가져온 사진 저장 요청")
data class SavePhotosFromQrRequest(
    @Schema(description = "사진 URL 목록")
    val photoUrls: List<String>,

    @Schema(description = "사진관 ID (매칭된 경우)")
    val photoBoothId: String?,

    @Schema(description = "앨범명")
    val albumName: String,

    @Schema(description = "사진관 정보 (매칭되지 않은 경우)")
    val photoBoothInfo: PhotoBoothInfo?
)

@Schema(description = "QR 사진 저장 응답")
data class SavePhotosFromQrResponse(
    @Schema(description = "생성된 앨범 ID")
    val albumId: String,

    @Schema(description = "앨범 이름")
    val albumName: String,

    @Schema(description = "저장된 사진 개수")
    val savedPhotoCount: Int,

    @Schema(description = "저장된 사진 ID 목록")
    val photoIds: List<String>,

    @Schema(description = "방문 기록 추가 여부")
    val visitRecordAdded: Boolean,

    @Schema(description = "메시지")
    val message: String
)