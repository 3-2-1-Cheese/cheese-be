package com.hsmile.cheese321.api.auth.service

import com.hsmile.cheese321.api.auth.dto.*
import com.hsmile.cheese321.api.common.client.kakao.KakaoApiClient
import com.hsmile.cheese321.api.common.client.kakao.KakaoUserInfoResponse
import com.hsmile.cheese321.api.common.exception.*
import com.hsmile.cheese321.api.common.security.JwtTokenProvider
import com.hsmile.cheese321.data.user.entity.User
import com.hsmile.cheese321.data.user.repository.UserRepository
import com.hsmile.cheese321.data.photo.entity.Album
import com.hsmile.cheese321.data.photo.repository.AlbumRepository
import org.slf4j.LoggerFactory
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
    private val albumRepository: AlbumRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val kakaoApiClient: KakaoApiClient
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    // 개발용 토큰 무효화를 위한 메모리 저장소 (로컬 전용)
    private val invalidatedDevTokens = ConcurrentHashMap.newKeySet<String>()

    /**
     * 카카오 로그인 - 예외 처리 개선
     */
    fun loginWithKakao(accessToken: String): AuthTokenResponse {
        try {
            logger.info("Starting Kakao login process")

            // 1. 카카오 API로 사용자 정보 조회
            val kakaoUserInfo = kakaoApiClient.fetchUserInfo(accessToken)
            val kakaoId = kakaoUserInfo.id
            val nickname = extractNickname(kakaoUserInfo) ?: "치즈_${kakaoId.toString().takeLast(4)}"

            logger.debug("Kakao user info retrieved: kakaoId={}, nickname={}", kakaoId, nickname)

            // 2. 사용자 정보 조회 또는 생성
            val user = userRepository.findByKakaoId(kakaoId)
                ?: createNewUserWithDefaultAlbum(kakaoId, nickname)

            // 3. 기존 사용자면 정보 업데이트
            if (user.nickname != nickname) {
                user.updateNickname(nickname)
                userRepository.save(user)
                logger.debug("Updated user nickname: {}", nickname)
            }

            // 4. JWT 토큰 발급
            val newAccessToken = try {
                jwtTokenProvider.generateAccessToken(user.id)
            } catch (e: Exception) {
                logger.error("Failed to generate access token for user: ${user.id}", e)
                throw AuthException(AuthErrorCode.TOKEN_GENERATION_FAILED, "액세스 토큰 생성에 실패했습니다", e)
            }

            val refreshToken = try {
                jwtTokenProvider.generateRefreshToken(user.id)
            } catch (e: Exception) {
                logger.error("Failed to generate refresh token for user: ${user.id}", e)
                throw AuthException(AuthErrorCode.TOKEN_GENERATION_FAILED, "리프레시 토큰 생성에 실패했습니다", e)
            }

            // 5. Refresh Token 저장
            try {
                user.updateRefreshToken(refreshToken)
                userRepository.save(user)
            } catch (e: Exception) {
                logger.error("Failed to save refresh token for user: ${user.id}", e)
                throw AuthException(AuthErrorCode.AUTH_SERVER_ERROR, "사용자 정보 저장에 실패했습니다", e)
            }

            logger.info("Kakao login completed successfully for user: {}", user.id)

            return AuthTokenResponse(
                accessToken = newAccessToken,
                refreshToken = refreshToken,
                user = user.toUserResponse()
            )

        } catch (e: AuthException) {
            // 이미 우리가 정의한 예외는 그대로 던지기
            logger.warn("Kakao login failed: {}", e.message)
            throw e
        } catch (e: Exception) {
            // 예상하지 못한 예외는 일반적인 인증 실패로 처리
            logger.error("Unexpected error during Kakao login", e)
            throw AuthException(AuthErrorCode.AUTHENTICATION_FAILED, "로그인 처리 중 오류가 발생했습니다", e)
        }
    }

    /**
     * 토큰 갱신 - 예외 처리 개선
     */
    fun refreshToken(refreshToken: String): AuthTokenResponse {
        try {
            logger.debug("Starting token refresh process")

            // 1. Refresh Token 기본 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                logger.warn("Invalid refresh token provided")
                throw RefreshTokenException.invalid()
            }

            // 2. 토큰 타입 확인
            val tokenType = try {
                jwtTokenProvider.getTokenType(refreshToken)
            } catch (e: Exception) {
                logger.warn("Failed to get token type from refresh token", e)
                throw RefreshTokenException.invalid()
            }

            if (tokenType != "refresh") {
                logger.warn("Token type is not refresh: {}", tokenType)
                throw RefreshTokenException.invalid()
            }

            // 3. 사용자 조회
            val userId = try {
                jwtTokenProvider.getUserIdFromToken(refreshToken)
            } catch (e: Exception) {
                logger.warn("Failed to extract user ID from refresh token", e)
                throw RefreshTokenException.invalid()
            }

            val user = userRepository.findById(userId)
                .orElseThrow {
                    logger.warn("User not found for refresh token: {}", userId)
                    UserException.notFound(userId)
                }

            // 4. 저장된 Refresh Token과 비교
            if (user.refreshToken.isNullOrBlank()) {
                logger.warn("No refresh token stored for user: {}", userId)
                throw RefreshTokenException.notFound()
            }

            if (user.refreshToken != refreshToken) {
                logger.warn("Refresh token mismatch for user: {}", userId)
                throw RefreshTokenException.mismatch()
            }

            // 5. 새로운 토큰 발급
            val newAccessToken = try {
                jwtTokenProvider.generateAccessToken(userId)
            } catch (e: Exception) {
                logger.error("Failed to generate new access token for user: {}", userId, e)
                throw AuthException(AuthErrorCode.TOKEN_GENERATION_FAILED, "새로운 액세스 토큰 생성에 실패했습니다", e)
            }

            val newRefreshToken = try {
                jwtTokenProvider.generateRefreshToken(userId)
            } catch (e: Exception) {
                logger.error("Failed to generate new refresh token for user: {}", userId, e)
                throw AuthException(AuthErrorCode.TOKEN_GENERATION_FAILED, "새로운 리프레시 토큰 생성에 실패했습니다", e)
            }

            // 6. 새로운 Refresh Token 저장
            try {
                user.updateRefreshToken(newRefreshToken)
                userRepository.save(user)
            } catch (e: Exception) {
                logger.error("Failed to save new refresh token for user: {}", userId, e)
                throw AuthException(AuthErrorCode.AUTH_SERVER_ERROR, "토큰 정보 저장에 실패했습니다", e)
            }

            logger.info("Token refresh completed successfully for user: {}", userId)

            return AuthTokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                user = user.toUserResponse()
            )

        } catch (e: AuthException) {
            // 이미 우리가 정의한 예외는 그대로 던지기
            throw e
        } catch (e: Exception) {
            // 예상하지 못한 예외
            logger.error("Unexpected error during token refresh", e)
            throw AuthException(AuthErrorCode.AUTH_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다", e)
        }
    }

    /**
     * 로그아웃
     */
    fun logout(userId: String) {
        try {
            val user = userRepository.findById(userId).orElse(null)

            user?.let {
                it.updateRefreshToken(null)
                userRepository.save(it)
            }

            logger.info("User logged out successfully: {}", userId)
        } catch (e: Exception) {
            // 로그아웃은 실패해도 성공으로 처리 (클라이언트에서 토큰 삭제하면 됨)
            logger.warn("Failed to update logout info for user: {}", userId, e)
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
     * 새로운 사용자 생성 + 기본 앨범 자동 생성
     */
    private fun createNewUserWithDefaultAlbum(kakaoId: Long, nickname: String): User {
        logger.info("Creating new user: kakaoId={}, nickname={}", kakaoId, nickname)

        // 1. 사용자 생성
        val user = User(
            id = UUID.randomUUID().toString(),
            kakaoId = kakaoId,
            nickname = nickname,
            profileImageUrl = null,
            createdAt = LocalDateTime.now()
        )
        val savedUser = userRepository.save(user)

        // 2. 기본 앨범 생성 ("전체 사진")
        val defaultAlbum = Album.createDefaultAlbum(savedUser.id)
        albumRepository.save(defaultAlbum)

        logger.info("New user created with default album: userId={}", savedUser.id)

        return savedUser
    }

    /**
     * 기존 사용자의 기본 앨범 확인 및 생성 (마이그레이션용)
     */
    fun ensureDefaultAlbumExists(userId: String) {
        val existingDefaultAlbum = albumRepository.findByUserIdAndIsDefaultTrue(userId)

        if (existingDefaultAlbum == null) {
            val defaultAlbum = Album.createDefaultAlbum(userId)
            albumRepository.save(defaultAlbum)
            logger.info("Created missing default album for user: {}", userId)
        }
    }

    /**
     * 카카오 사용자 정보에서 닉네임 추출
     */
    private fun extractNickname(kakaoUserInfo: KakaoUserInfoResponse): String? {
        return kakaoUserInfo.properties?.nickname
            ?: kakaoUserInfo.kakao_account?.profile?.nickname
    }
}