package com.hsmile.cheese321.api.qr.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "QR 스캔 요청")
data class QrScanRequest(
    @Schema(description = "QR 코드 데이터")
    val qrData: String
)

@Schema(description = "QR 스캔 응답")
data class QrScanResponse(
    @Schema(description = "스캔 성공 여부")
    val success: Boolean,

    @Schema(description = "응답 메시지")
    val message: String,

    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "사진관 이름")
    val photoBoothName: String,

    @Schema(description = "사진관 주소")
    val photoBoothAddress: String

    // TODO: 나중에 필요하면 추가할 것들
    // - 사진관 이미지 URL
    // - 사진관 운영시간
    // - 부스 개수/종류 정보
)