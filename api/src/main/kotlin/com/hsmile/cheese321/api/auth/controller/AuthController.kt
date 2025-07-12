package com.hsmile.cheese321.api.auth.controller

import com.hsmile.cheese321.api.auth.dto.*
import com.hsmile.cheese321.api.auth.service.AuthService
import com.hsmile.cheese321.api.auth.spec.AuthApi
import org.springframework.context.annotation.Profile
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 컨트롤러
 */
@RestController
class AuthController(
    private val authService: AuthService
) : AuthApi {

    /**
     * 카카오 로그인
     */
    override fun loginWithKakao(request: KakaoLoginRequest): ResponseEntity<AuthTokenResponse> {
        val response = authService.loginWithKakao(request.accessToken)
        return ResponseEntity.ok(response)
    }

    /**
     * 토큰 갱신
     */
    override fun refreshToken(request: RefreshTokenRequest): ResponseEntity<AuthTokenResponse> {
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    /**
     * 로그아웃
     */
    override fun logout(@AuthenticationPrincipal userId: String): ResponseEntity<Unit> {
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }

    // ===== 개발용 API =====

    /**
     * 개발용 토큰 발급
     */
    @Profile("local", "dev")
    override fun generateDevToken(userId: String): ResponseEntity<DevTokenResponse> {
        val response = authService.generateDevToken(userId)
        return ResponseEntity.ok(response)
    }

    /**
     * 개발용 토큰 무효화
     */
    @Profile("local", "dev")
    override fun invalidateDevTokens(): ResponseEntity<Unit> {
        authService.invalidateDevTokens()
        return ResponseEntity.ok().build()
    }

    /**
     * 개발 모드 상태 확인
     */
    @Profile("local", "dev")
    override fun getDevStatus(): ResponseEntity<DevStatusResponse> {
        val response = authService.getDevStatus()
        return ResponseEntity.ok(response)
    }
}