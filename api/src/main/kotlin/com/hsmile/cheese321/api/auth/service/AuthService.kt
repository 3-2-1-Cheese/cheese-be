package com.hsmile.cheese321.api.auth.service

import com.hsmile.cheese321.api.auth.dto.*
import com.hsmile.cheese321.api.common.client.kakao.KakaoApiClient
import com.hsmile.cheese321.api.common.client.kakao.KakaoUserInfoResponse
import com.hsmile.cheese321.api.common.exception.AuthException
import com.hsmile.cheese321.api.common.security.JwtTokenProvider
import com.hsmile.cheese321.data.user.entity.User
import com.hsmile.cheese321.data.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 인증 서비스
 */
@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val kakaoApiClient: KakaoApiClient
) {

    // 개발용 토큰 무효화를 위한 메모리 저장소 (로컬 전용)
    private val invalidatedDevTokens = ConcurrentHashMap.newKeySet<String>()

    /**
     * 카카오 로그인
     */
    fun loginWithKakao(accessToken: String): AuthTokenResponse {
        // 1. 카카오 API로 사용자 정보 조회
        val kakaoUserInfo = kakaoApiClient.fetchUserInfo(accessToken)
        val kakaoId = kakaoUserInfo.id
        val nickname = extractNickname(kakaoUserInfo) ?: "치즈_${kakaoId.toString().takeLast(4)}"

        // 2. 사용자 정보 조회 또는 생성
        val user = userRepository.findByKakaoId(kakaoId)
            ?: createNewUser(kakaoId, nickname)

        // 3. 기존 사용자면 정보 업데이트
        if (user.nickname != nickname) {
            user.updateNickname(nickname)
            userRepository.save(user)
        }

        // 4. JWT 토큰 발급
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        // 5. Refresh Token 저장
        user.updateRefreshToken(refreshToken)
        userRepository.save(user)

        return AuthTokenResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            user = user.toUserResponse()
        )
    }

    /**
     * 토큰 갱신
     */
    fun refreshToken(refreshToken: String): AuthTokenResponse {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw AuthException("유효하지 않은 Refresh Token입니다")
        }

        val tokenType = jwtTokenProvider.getTokenType(refreshToken)
        if (tokenType != "refresh") {
            throw AuthException("Refresh Token이 아닙니다")
        }

        // 2. 사용자 조회
        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException("존재하지 않는 사용자입니다") }

        // 3. 저장된 Refresh Token과 비교
        if (user.refreshToken != refreshToken) {
            throw AuthException("유효하지 않은 Refresh Token입니다")
        }

        // 4. 새로운 토큰 발급
        val newAccessToken = jwtTokenProvider.generateAccessToken(userId)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(userId)

        // 5. 새로운 Refresh Token 저장
        user.updateRefreshToken(newRefreshToken)
        userRepository.save(user)

        return AuthTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            user = user.toUserResponse()
        )
    }

    /**
     * 로그아웃
     */
    fun logout(userId: String) {
        try {
            val user = userRepository.findById(userId).orElse(null)

            // Refresh Token 제거
            user?.let {
                it.updateRefreshToken(null)
                userRepository.save(it)
            }
        } catch (e: Exception) {
            // 사용자를 찾을 수 없어도 로그아웃은 성공으로 처리
        }
    }

    // ===== 개발용 기능들 =====

    /**
     * 개발용 토큰 발급
     */
    fun generateDevToken(userId: String): DevTokenResponse {
        val devToken = jwtTokenProvider.generateDevToken(userId)

        return DevTokenResponse(
            accessToken = devToken,
            userId = userId,
            expiresAt = "무제한",
            message = "개발용 토큰이 발급되었습니다. 이 토큰은 로컬 환경에서만 유효합니다."
        )
    }

    /**
     * 개발용 토큰 무효화
     */
    fun invalidateDevTokens() {
        // 메모리에서 모든 개발 토큰을 무효화 처리
        invalidatedDevTokens.clear()
    }

    /**
     * 개발 상태 확인
     */
    fun getDevStatus(): DevStatusResponse {
        return DevStatusResponse(
            enabled = true,
            environment = "local",
            invalidatedTokensCount = invalidatedDevTokens.size,
            message = "개발 모드가 활성화되어 있습니다."
        )
    }

    // ===== 내부 헬퍼 메서드들 =====

    /**
     * 새로운 사용자 생성
     */
    private fun createNewUser(kakaoId: Long, nickname: String): User {
        val user = User(
            id = UUID.randomUUID().toString(),
            kakaoId = kakaoId,
            nickname = nickname,
            profileImageUrl = null,
            createdAt = LocalDateTime.now()
        )
        return userRepository.save(user)
    }

    /**
     * 카카오 사용자 정보에서 닉네임 추출
     */
    private fun extractNickname(kakaoUserInfo: KakaoUserInfoResponse): String? {
        return kakaoUserInfo.properties?.nickname
    }
}