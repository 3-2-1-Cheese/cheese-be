package com.hsmile.cheese321.api.spec

import com.hsmile.cheese321.api.dto.response.PhotoBoothResponse
import com.hsmile.cheese321.api.dto.response.PhotoBoothDetailResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "PhotoBooth", description = "사진관 API")
@RequestMapping(PhotoBoothUris.BASE)
interface PhotoBoothApi {

    @Operation(summary = "사진관 목록 조회", description = "지역, 브랜드, 키워드 기반 사진관 검색")
    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진관 목록 조회 성공")
        ]
    )
    fun getPhotoBooths(
        @RequestParam region: String?,           // 강남역, 잠실역, 건대입구역, 홍대입구역
        @RequestParam brand: String?,            // 인생네컷, 포토이즘박스 등
        @RequestParam keywords: List<String>?,   // 사용자 선호 키워드 매칭
        @RequestParam lat: Double?,              // 현재 위치 위도
        @RequestParam lng: Double?,              // 현재 위치 경도
        @RequestParam radius: Int? = 1000        // 검색 반경(미터)
    ): List<PhotoBoothResponse>

    @Operation(summary = "사진관 상세 조회", description = "사진관 상세 정보 및 키워드 하이라이트")
    @GetMapping(PhotoBoothUris.DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진관 상세 조회 성공"),
            ApiResponse(responseCode = "404", description = "사진관을 찾을 수 없음")
        ]
    )
    fun getPhotoBoothDetail(@PathVariable id: String): PhotoBoothDetailResponse
}