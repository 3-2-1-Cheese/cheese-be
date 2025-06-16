package com.hsmile.cheese321.api.qr

import com.hsmile.cheese321.api.qr.dto.QrScanRequest
import com.hsmile.cheese321.api.qr.dto.QrScanResponse
import com.hsmile.cheese321.api.qr.dto.PhotoPreviewResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "QR", description = "QR 코드 스캔 및 사진 처리 API")
@RequestMapping(QrUris.BASE)
interface QrApi {

    @Operation(summary = "QR 코드 스캔", description = "QR 코드에서 사진 URL 추출 및 검증")
    @PostMapping(QrUris.SCAN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "QR 스캔 성공"),
            ApiResponse(responseCode = "400", description = "유효하지 않은 QR 코드"),
            ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음")
        ]
    )
    fun scanQrCode(@RequestBody request: QrScanRequest): QrScanResponse

    @Operation(summary = "사진 미리보기 조회", description = "스캔된 사진의 상세 정보 및 미리보기")
    @GetMapping(QrUris.PREVIEW)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "미리보기 조회 성공"),
            ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음"),
            ApiResponse(responseCode = "410", description = "만료된 URL")
        ]
    )
    fun getPhotoPreview(@RequestParam scanId: String): PhotoPreviewResponse
}