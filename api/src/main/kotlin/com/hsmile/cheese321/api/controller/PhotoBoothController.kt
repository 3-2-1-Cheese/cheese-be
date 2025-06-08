package com.hsmile.cheese321.api.controller

import com.hsmile.cheese321.api.dto.response.PhotoBoothResponse
import com.hsmile.cheese321.api.dto.response.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.spec.PhotoBoothApi
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
        // - 사진관 상세 정보 조회
        // - 사용자 선호 키워드와 매칭
        // - 키워드 하이라이트 처리
        // - 거리 계산
        throw NotImplementedError("사진관 상세 조회 - 아직 구현 안됨")
    }
}