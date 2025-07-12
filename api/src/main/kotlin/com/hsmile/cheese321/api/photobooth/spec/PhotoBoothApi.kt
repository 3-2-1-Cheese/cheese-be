package com.hsmile.cheese321.api.photobooth.spec

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * PhotoBooth API URI 상수
 */
object PhotoBoothUris {
    const val BASE = "/api/v1/photobooths"
    const val DETAIL = "/{id}"
}

/**
 * 사진관 정보 API
 */
@Tag(name = "PhotoBooth", description = "사진관 정보 API")
interface PhotoBoothApi {

    @Operation(summary = "사진관 목록 조회", description = "위치 기반 사진관 검색 (개인화 정보 포함)")
    @GetMapping(PhotoBoothUris.BASE)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진관 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getPhotoBooths(
        @AuthenticationPrincipal userId: String,    // 개인화를 위한 사용자 ID
        @RequestParam lat: Double?,                 // 현재 위치 위도 (선택)
        @RequestParam lng: Double?,                 // 현재 위치 경도 (선택)
        @RequestParam radius: Int? = 1000,          // 검색 반경(미터)
        @RequestParam region: String?,              // 강남역, 잠실역, 건대입구역, 홍대입구역
        @RequestParam brand: String?,               // 인생네컷, 포토이즘박스 등
        @RequestParam keyword: String?              // 통합 검색 (사진관명/브랜드명/지역)
    ): List<PhotoBoothResponse>

    @Operation(summary = "사진관 상세 정보", description = "특정 사진관의 상세 정보 조회 (개인화 정보 포함)")
    @GetMapping(PhotoBoothUris.BASE + PhotoBoothUris.DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진관 상세 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사진관을 찾을 수 없음")
        ]
    )
    fun getPhotoBoothDetail(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): PhotoBoothDetailResponse
}