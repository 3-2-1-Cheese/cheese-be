package com.hsmile.cheese321.api.auth.spec

import com.hsmile.cheese321.api.auth.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


/**
 * 사용자 인증 API
 */
@Tag(name = "Auth", description = "사용자 인증 API")
interface AuthApi {

    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인")
    @PostMapping(AuthUris.BASE + AuthUris.KAKAO_LOGIN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 카카오 토큰"),
            ApiResponse(responseCode = "500", description = "카카오 API 오류")
        ]
    )
    fun loginWithKakao(@RequestBody request: KakaoLoginRequest): ResponseEntity<AuthTokenResponse>

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token 발급")
    @PostMapping(AuthUris.BASE + AuthUris.REFRESH)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
        ]
    )
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthTokenResponse>

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 (Refresh Token 무효화)")
    @PostMapping(AuthUris.BASE + AuthUris.LOGOUT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun logout(@AuthenticationPrincipal userId: String): ResponseEntity<Unit>

    // ===== 개발용 API =====

    @Operation(summary = "[개발용] 토큰 발급", description = "개발용 무제한 토큰 발급")
    @PostMapping(AuthUris.BASE + AuthUris.DEV_TOKEN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "개발용 토큰 발급 성공")
        ]
    )
    fun generateDevToken(@RequestParam(defaultValue = "dev-user-123") userId: String): ResponseEntity<DevTokenResponse>

    @Operation(summary = "[개발용] 토큰 무효화", description = "발급된 개발용 토큰 무효화")
    @DeleteMapping(AuthUris.BASE + AuthUris.DEV_TOKEN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 무효화 성공")
        ]
    )
    fun invalidateDevTokens(): ResponseEntity<Unit>

    @Operation(summary = "[개발용] 개발 모드 상태", description = "현재 개발 토큰 상태 확인")
    @GetMapping(AuthUris.BASE + AuthUris.DEV_STATUS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "상태 조회 성공")
        ]
    )
    fun getDevStatus(): ResponseEntity<DevStatusResponse>

    /**
     * Auth API URI 상수
     */
    object AuthUris {
        const val BASE = "/api/v1/auth"
        const val KAKAO_LOGIN = "/kakao/login"
        const val REFRESH = "/refresh"
        const val LOGOUT = "/logout"

        // 개발용 엔드포인트
        const val DEV_TOKEN = "/dev/token"
        const val DEV_STATUS = "/dev/status"
    }
}