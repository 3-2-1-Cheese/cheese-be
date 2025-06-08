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

    @Operation(summary = "사진관 목록 조회", description = "위치 기반 사진관 검색 (지도/내주변용)")
    @GetMapping
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진관 목록 조회 성공")
        ]
    )
    fun getPhotoBooths(
        @RequestParam lat: Double,               // 현재 위치 위도 (필수)
        @RequestParam lng: Double,               // 현재 위치 경도 (필수)
        @RequestParam radius: Int? = 1000,       // 검색 반경(미터)
        @RequestParam region: String?,           // 강남역, 잠실역, 건대입구역, 홍대입구역
        @RequestParam brand: String?             // 인생네컷, 포토이즘박스 등
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