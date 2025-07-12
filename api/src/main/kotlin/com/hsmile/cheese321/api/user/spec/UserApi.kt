package com.hsmile.cheese321.api.user.spec

import com.hsmile.cheese321.api.user.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * User API URI 상수
 */
object UserUris {
    const val BASE = "/api/v1/users"
    const val ME = "/me"
    const val KEYWORDS = "/me/keywords"
    const val FAVORITES = "/me/favorites"
}

/**
 * 사용자 정보 관리 API
 */
@Tag(name = "User", description = "사용자 정보 관리 API")
interface UserApi {

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보 조회")
    @GetMapping(UserUris.BASE + UserUris.ME)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
        ]
    )
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse>

    @Operation(summary = "선호 키워드 설정", description = "사용자의 선호 키워드 설정/업데이트")
    @PutMapping(UserUris.BASE + UserUris.KEYWORDS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "키워드 설정 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "400", description = "잘못된 키워드 형식")
        ]
    )
    fun updatePreferredKeywords(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: UpdatePreferredKeywordsRequest
    ): ResponseEntity<PreferredKeywordsResponse>

    @Operation(summary = "선호 키워드 조회", description = "사용자가 설정한 선호 키워드 목록 조회")
    @GetMapping(UserUris.BASE + UserUris.KEYWORDS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "키워드 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getPreferredKeywords(@AuthenticationPrincipal userId: String): ResponseEntity<PreferredKeywordsResponse>

    // ===== 찜하기 기능 API =====

    @Operation(summary = "관심 사진관 목록 조회", description = "내가 찜한 사진관 목록 조회")
    @GetMapping(UserUris.BASE + UserUris.FAVORITES)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "관심 목록 조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getFavoritePhotoBooths(@AuthenticationPrincipal userId: String): ResponseEntity<FavoritePhotoBoothsResponse>
}