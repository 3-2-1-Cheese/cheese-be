package com.hsmile.cheese321.api.qr

import com.hsmile.cheese321.api.qr.dto.QrScanRequest
import com.hsmile.cheese321.api.qr.dto.QrScanResponse
import com.hsmile.cheese321.api.qr.dto.PhotoPreviewResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class QrController : QrApi {

    override fun scanQrCode(request: QrScanRequest): QrScanResponse {
        // TODO: 실제 구현
        // - QR 코드 데이터에서 URL 추출
        // - URL 유효성 검증
        // - 사진 메타데이터 추출
        // - 임시 다운로드 및 스캔 ID 생성
        throw NotImplementedError("QR 코드 스캔 - 아직 구현 안됨")
    }

    override fun getPhotoPreview(scanId: String): PhotoPreviewResponse {
        // TODO: 실제 구현
        // - 스캔 ID로 임시 저장된 사진 조회
        // - 미리보기 이미지 URL 생성
        // - 개별 사진 분할 정보 제공
        throw NotImplementedError("사진 미리보기 - 아직 구현 안됨")
    }
}