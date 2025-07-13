package com.hsmile.cheese321.api.qr.controller

import com.hsmile.cheese321.api.qr.dto.*
import com.hsmile.cheese321.api.qr.service.QrService
import com.hsmile.cheese321.api.qr.spec.QrApi
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid

/**
 * QR 스캔 및 사진 저장 컨트롤러
 */
@RestController
class QrController(
    private val qrService: QrService
) : QrApi {

    /**
     * QR 코드 스캔
     */
    override fun scanQr(
        @AuthenticationPrincipal userId: String,
        @Valid request: QrScanRequest
    ): ResponseEntity<QrScanResponse> {
        val response = qrService.scanQr(userId, request)
        return ResponseEntity.ok(response)
    }

    /**
     * QR로 가져온 사진 저장
     */
    override fun savePhotosFromQr(
        @AuthenticationPrincipal userId: String,
        @Valid request: SavePhotosFromQrRequest
    ): ResponseEntity<SavePhotosFromQrResponse> {
        val response = qrService.savePhotosFromQr(userId, request)
        return ResponseEntity.ok(response)
    }
}