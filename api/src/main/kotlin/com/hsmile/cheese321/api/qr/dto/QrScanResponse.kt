package com.hsmile.cheese321.api.qr.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "QR 스캔 응답")
data class QrScanResponse(
    @Schema(description = "스캔 ID (임시 저장 식별자)")
    val scanId: String,

    @Schema(description = "스캔 상태", example = "SUCCESS")
    val status: QrScanStatus,

    @Schema(description = "추출된 사진 URL")
    val photoUrl: String,

    @Schema(description = "사진 기본 정보")
    val photoInfo: PhotoInfoResponse,

    @Schema(description = "스캔 시간")
    val scannedAt: String,

    @Schema(description = "만료 시간 (임시 저장)")
    val expiresAt: String
)

@Schema(description = "사진 기본 정보")
data class PhotoInfoResponse(
    @Schema(description = "파일 크기 (bytes)")
    val fileSize: Long,

    @Schema(description = "이미지 너비")
    val width: Int,

    @Schema(description = "이미지 높이")
    val height: Int,

    @Schema(description = "파일 형식", example = "JPEG")
    val format: String,

    @Schema(description = "예상 사진 타입", example = "FOUR_CUT")
    val photoType: PhotoType
)

@Schema(description = "QR 스캔 상태")
enum class QrScanStatus {
    @Schema(description = "스캔 성공")
    SUCCESS,

    @Schema(description = "유효하지 않은 QR")
    INVALID_QR,

    @Schema(description = "사진 다운로드 실패")
    DOWNLOAD_FAILED,

    @Schema(description = "지원하지 않는 형식")
    UNSUPPORTED_FORMAT
}

@Schema(description = "사진 타입")
enum class PhotoType {
    @Schema(description = "인생네컷 (4분할)")
    FOUR_CUT,

    @Schema(description = "일반 사진")
    SINGLE,

    @Schema(description = "기타")
    OTHER
}