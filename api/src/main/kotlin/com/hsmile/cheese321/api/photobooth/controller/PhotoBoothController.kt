package com.hsmile.cheese321.api.photobooth.controller

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothService
import com.hsmile.cheese321.api.photobooth.spec.PhotoBoothApi
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RestController

/**
 * 사진관 정보 컨트롤러
 */
@RestController
class PhotoBoothController(
    private val photoBoothService: PhotoBoothService
) : PhotoBoothApi {

    /**
     * 사진관 목록 조회
     */
    override fun getPhotoBooths(
        @AuthenticationPrincipal userId: String,
        lat: Double?,
        lng: Double?,
        radius: Int?,
        region: String?,
        brand: String?,
        keyword: String?
    ): List<PhotoBoothResponse> {
        return photoBoothService.getPhotoBooths(lat, lng, radius, region, brand, keyword)
    }

    /**
     * 사진관 상세 조회
     */
    override fun getPhotoBoothDetail(
        @AuthenticationPrincipal userId: String,
        id: String
    ): PhotoBoothDetailResponse {
        return photoBoothService.getPhotoBoothDetail(id)
    }
}