package com.hsmile.cheese321.api.qr.spec

import com.hsmile.cheese321.api.qr.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * QR URI 상수
 */
object QrUris {
    const val BASE = "/api/v1/qr"
    const val SCAN = "/scan"
    const val SAVE_PHOTOS = "/save-photos"
}

/**
 * QR 스캔 및 사진 저장 API
 */
@Tag(name = "QR", description = "QR 스캔 및 사진 저장 API")
interface QrApi {

    @Operation(summary = "QR 코드 스캔", description = "QR 코드를 스캔하여 사진 URL이나 사진관 정보 추출")
    @PostMapping(QrUris.BASE + QrUris.SCAN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "QR 스캔 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "400", description = "잘못된 QR 데이터")
        ]
    )
    fun scanQr(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: QrScanRequest
    ): ResponseEntity<QrScanResponse>

    @Operation(summary = "QR로 가져온 사진 저장", description = "QR 스캔으로 얻은 사진들을 앱에 저장")
    @PostMapping(QrUris.BASE + QrUris.SAVE_PHOTOS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 저장 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "500", description = "사진 다운로드 실패")
        ]
    )
    fun savePhotosFromQr(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: SavePhotosFromQrRequest
    ): ResponseEntity<SavePhotosFromQrResponse>
}