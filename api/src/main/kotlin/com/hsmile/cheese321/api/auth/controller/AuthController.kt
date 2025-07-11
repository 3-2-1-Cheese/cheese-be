package com.hsmile.cheese321.api.auth.controller

import com.hsmile.cheese321.api.auth.dto.*
import com.hsmile.cheese321.api.auth.service.AuthService
import com.hsmile.cheese321.api.auth.spec.AuthApi
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 API Controller
 * 카카오 로그인, 토큰 갱신, 로그아웃 기능 제공
 */
@RestController
class AuthController(
    private val authService: AuthService
) : AuthApi {

    override fun kakaoLogin(request: KakaoLoginRequest): ResponseEntity<AuthTokenResponse> =
        ResponseEntity.ok(authService.loginWithKakao(request.accessToken))

    override fun refreshToken(request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> =
        ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken))

    override fun logout(userId: String): ResponseEntity<Void> {
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }
}