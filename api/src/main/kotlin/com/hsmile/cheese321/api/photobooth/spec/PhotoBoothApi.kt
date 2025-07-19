package com.hsmile.cheese321.api.photobooth.spec

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.dto.RatingDeleteResponse
import com.hsmile.cheese321.api.photobooth.dto.RatingRequest
import com.hsmile.cheese321.api.photobooth.dto.RatingResponse
import com.hsmile.cheese321.api.user.dto.FavoriteToggleResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * PhotoBooth API URI 상수
 */
object PhotoBoothUris {
    const val BASE = "/api/v1/photobooths"
    const val DETAIL = "/{id}"
    const val FAVORITE = "/{id}/favorite"
    const val RECOMMEND = "/recommended"
    const val RATING = "/{id}/rating"
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
        @AuthenticationPrincipal userId: String,
        @RequestParam lat: Double?,
        @RequestParam lng: Double?,
        @RequestParam radius: Int? = 1000,
        @RequestParam region: String?,
        @RequestParam brand: String?,
        @RequestParam keyword: String?
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

    // ===== 찜하기 기능 API =====

    @Operation(summary = "사진관 찜하기/취소", description = "사진관을 관심 목록에 추가하거나 제거")
    @PostMapping(PhotoBoothUris.BASE + PhotoBoothUris.FAVORITE)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "찜하기 상태 변경 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사진관을 찾을 수 없음")
        ]
    )
    fun toggleFavorite(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): ResponseEntity<FavoriteToggleResponse>

    @Operation(summary = "추천 사진관 목록", description = "사용자 선호도를 기반으로 한 개인화 추천 사진관")
    @GetMapping(PhotoBoothUris.BASE + PhotoBoothUris.RECOMMEND)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "추천 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getRecommendedPhotoBooths(
        @AuthenticationPrincipal userId: String,
        @RequestParam(required = false) lat: Double?,
        @RequestParam(required = false) lng: Double?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<PhotoBoothResponse>>

    // ===== 별점 관련 API =====
    @Operation(summary = "사진관 별점 등록/수정", description = "사진관에 1~5점 별점 등록 또는 기존 별점 수정")
    @PutMapping(PhotoBoothUris.BASE + PhotoBoothUris.RATING)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "별점 등록/수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 별점 값 (1~5점만 허용)"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사진관을 찾을 수 없음")
        ]
    )
    fun upsertRating(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String,
        @Valid @RequestBody request: RatingRequest
    ): ResponseEntity<RatingResponse>

    @Operation(summary = "사진관 별점 삭제", description = "사용자가 등록한 별점 삭제")
    @DeleteMapping(PhotoBoothUris.BASE + PhotoBoothUris.RATING)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "별점 삭제 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "별점을 찾을 수 없음")
        ]
    )
    fun deleteRating(
        @AuthenticationPrincipal userId: String,
        @PathVariable id: String
    ): ResponseEntity<RatingDeleteResponse>
}