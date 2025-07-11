package com.hsmile.cheese321.api.auth.spec

import com.hsmile.cheese321.api.auth.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 인증 API 스펙 정의
 */
@Tag(name = "Auth", description = "인증 API")
@RequestMapping(AuthUris.BASE)
interface AuthApi {

    @Operation(summary = "카카오 로그인", description = "카카오 Access Token으로 로그인/회원가입")
    @PostMapping(AuthUris.KAKAO_LOGIN)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패")
        ]
    )
    fun kakaoLogin(@RequestBody request: KakaoLoginRequest): ResponseEntity<AuthTokenResponse>

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access Token 갱신")
    @PostMapping(AuthUris.REFRESH)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
        ]
    )
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse>

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 (리프레시 토큰 삭제)")
    @PostMapping(AuthUris.LOGOUT)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun logout(@AuthenticationPrincipal userId: String): ResponseEntity<Void>
}

/**
 * Auth API URI 상수
 */
object AuthUris {
    const val BASE = "/api/v1/auth"
    const val KAKAO_LOGIN = "/kakao/login"
    const val REFRESH = "/refresh"
    const val LOGOUT = "/logout"
}