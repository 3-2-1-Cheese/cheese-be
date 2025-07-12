package com.hsmile.cheese321.api.qr.service

import com.hsmile.cheese321.api.qr.dto.QrScanRequest
import com.hsmile.cheese321.api.qr.dto.QrScanResponse
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * QR 스캔 관련 서비스
 */
@Service
@Transactional(readOnly = true)
class QrService(
    private val photoBoothRepository: PhotoBoothRepository
) {

    /**
     * QR 코드 스캔 - 사진관 정보 확인
     */
    fun scanQr(request: QrScanRequest): QrScanResponse {
        // QR 데이터에서 사진관 ID 추출
        // 실제로는 QR 코드 내용을 파싱해야 하지만,
        // 지금은 간단하게 photoBoothId가 직접 들어온다고 가정
        val photoBoothId = request.qrData

        val photoBooth = photoBoothRepository.findById(photoBoothId)
            .orElseThrow { IllegalArgumentException("유효하지 않은 QR 코드입니다") }

        return QrScanResponse(
            success = true,
            message = "QR 스캔 성공",
            photoBoothId = photoBooth.id,
            photoBoothName = photoBooth.name,
            photoBoothAddress = photoBooth.address
        )
    }
}