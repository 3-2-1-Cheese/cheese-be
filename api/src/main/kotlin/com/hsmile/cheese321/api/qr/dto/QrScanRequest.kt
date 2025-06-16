package com.hsmile.cheese321.api.qr.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "QR 스캔 요청")
data class QrScanRequest(
    @Schema(description = "QR 코드 데이터", example = "https://example.com/photos/abc123")
    val qrData: String,

    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val userId: String,

    @Schema(description = "스캔 위치 (선택사항)", example = "인생네컷 강남역점")
    val location: String? = null
)