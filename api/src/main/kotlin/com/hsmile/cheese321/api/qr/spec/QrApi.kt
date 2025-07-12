package com.hsmile.cheese321.api.qr.spec

import com.hsmile.cheese321.api.qr.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * QR API URI 상수
 */
object QrUris {
    const val BASE = "/api/v1/qr"
    const val SCAN = "/scan"
}

/**
 * QR 스캔 관련 API
 */
@Tag(name = "QR", description = "QR 코드 스캔 API")
interface QrApi {

    @Operation(summary = "QR 코드 스캔", description = "QR 코드를 스캔하여 사진관 정보 확인")
    @PostMapping(QrUris.BASE + QrUris.SCAN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "QR 스캔 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 QR 코드"),
            ApiResponse(responseCode = "404", description = "사진관을 찾을 수 없음")
        ]
    )
    fun scanQr(@RequestBody request: QrScanRequest): ResponseEntity<QrScanResponse>

    // TODO: 나중에 구현할 API들
    // - QR 코드 생성 (사진관용)
    // - QR 스캔 히스토리 조회
    // - QR 코드 유효성 검증
}