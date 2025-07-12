package com.hsmile.cheese321.api.qr.controller

import com.hsmile.cheese321.api.qr.dto.QrScanRequest
import com.hsmile.cheese321.api.qr.dto.QrScanResponse
import com.hsmile.cheese321.api.qr.service.QrService
import com.hsmile.cheese321.api.qr.spec.QrApi
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * QR 스캔 컨트롤러
 */
@RestController
class QrController(
    private val qrService: QrService
) : QrApi {

    /**
     * QR 코드 스캔
     */
    override fun scanQr(request: QrScanRequest): ResponseEntity<QrScanResponse> {
        val response = qrService.scanQr(request)
        return ResponseEntity.ok(response)
    }
}