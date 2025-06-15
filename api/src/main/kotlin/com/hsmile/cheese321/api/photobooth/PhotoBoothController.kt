package com.hsmile.cheese321.api.photobooth

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotoBoothController : PhotoBoothApi {

    override fun getPhotoBooths(
        lat: Double,
        lng: Double,
        radius: Int?,
        region: String?,
        brand: String?
    ): List<PhotoBoothResponse> {
        // TODO: 실제 서비스 로직 구현
        // - 거리 기반 정렬 (필수)
        // - 지역 필터링
        // - 브랜드 필터링
        throw NotImplementedError("사진관 목록 조회 - 아직 구현 안됨")
    }

    override fun getPhotoBoothDetail(id: String): PhotoBoothDetailResponse {
        // TODO: 실제 서비스 로직 구현
        // - 사진관 상세 정보
        // - 키워드 목록
        // - 리뷰 통계
        throw NotImplementedError("사진관 상세 조회 - 아직 구현 안됨")
    }
}