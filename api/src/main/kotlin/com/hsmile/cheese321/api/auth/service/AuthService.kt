package com.hsmile.cheese321.api.auth.service

import com.hsmile.cheese321.api.auth.client.KakaoApiClient
import com.hsmile.cheese321.api.auth.dto.AuthTokenResponse
import com.hsmile.cheese321.api.auth.dto.RefreshTokenResponse
import com.hsmile.cheese321.api.auth.dto.toUserResponse
import com.hsmile.cheese321.api.auth.jwt.JwtTokenProvider
import com.hsmile.cheese321.data.user.entity.User
import com.hsmile.cheese321.data.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 관련 비즈니스 로직 처리 서비스
 * 카카오 로그인, JWT 토큰 관리
 */
@Service
@Transactional
class AuthService(
    private val kakaoApiClient: KakaoApiClient,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * 카카오 로그인 처리
     * @param kakaoAccessToken 카카오 Access Token
     * @return 인증 토큰 및 사용자 정보
     */
    fun loginWithKakao(kakaoAccessToken: String): AuthTokenResponse {
        logger.info("카카오 로그인 시작")

        // 1. 카카오 API로 사용자 정보 조회
        val kakaoUserInfo = kakaoApiClient.fetchUserInfo(kakaoAccessToken)
        val kakaoId = kakaoUserInfo.id

        // 2. 닉네임 추출 (기본값 설정으로 안정성 확보)
        val nickname = extractNickname(kakaoUserInfo)
            ?: "치즈_${kakaoId.toString().takeLast(4)}"

        // 3. 프로필 이미지 추출
        val profileImageUrl = extractProfileImageUrl(kakaoUserInfo)

        // 4. 기존 사용자 조회 또는 신규 생성
        val (user, isNewUser) = userRepository.findByKakaoId(kakaoId)?.let { existingUser ->
            logger.info("기존 사용자 로그인: kakaoId={}", kakaoId)
            // 기존 사용자 정보 업데이트
            existingUser.updateProfile(nickname, profileImageUrl)
            existingUser to false
        } ?: run {
            logger.info("신규 사용자 가입: kakaoId={}", kakaoId)
            // 신규 사용자 생성
            val newUser = User(
                kakaoId = kakaoId,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
            userRepository.save(newUser) to true
        }

        // 5. JWT 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(user.id)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        // 6. 리프레시 토큰 저장
        user.updateRefreshToken(refreshToken)

        logger.info("카카오 로그인 성공: userId={}, isNewUser={}", user.id, isNewUser)

        return AuthTokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser,
            user = user.toUserResponse()
        )
    }

    /**
     * 리프레시 토큰으로 Access Token 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 Access Token
     */
    fun refreshAccessToken(refreshToken: String): RefreshTokenResponse {
        logger.info("토큰 갱신 요청")

        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw AuthException("유효하지 않은 리프레시 토큰입니다")
        }

        // 2. 토큰 타입 확인
        if (jwtTokenProvider.getTokenType(refreshToken) != "refresh") {
            throw AuthException("리프레시 토큰이 아닙니다")
        }

        // 3. DB에서 해당 리프레시 토큰을 가진 사용자 조회
        val user = userRepository.findByRefreshToken(refreshToken)
            ?: throw AuthException("유효하지 않은 리프레시 토큰입니다")

        // 4. 새로운 Access Token 생성
        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id)

        logger.info("토큰 갱신 성공: userId={}", user.id)

        return RefreshTokenResponse(accessToken = newAccessToken)
    }

    /**
     * 로그아웃 처리 (리프레시 토큰 삭제)
     * @param userId 사용자 ID
     */
    fun logout(userId: String) {
        logger.info("로그아웃 요청: userId={}", userId)

        userRepository.findById(userId).ifPresent { user ->
            user.updateRefreshToken(null)
            logger.info("로그아웃 성공: userId={}", userId)
        }
    }

    /**
     * 카카오 사용자 정보에서 닉네임 추출
     */
    private fun extractNickname(kakaoUserInfo: com.hsmile.cheese321.api.auth.dto.KakaoUserInfoResponse): String? {
        return kakaoUserInfo.properties?.nickname
            ?: kakaoUserInfo.kakaoAccount?.profile?.nickname
    }

    /**
     * 카카오 사용자 정보에서 프로필 이미지 URL 추출
     */
    private fun extractProfileImageUrl(kakaoUserInfo: com.hsmile.cheese321.api.auth.dto.KakaoUserInfoResponse): String? {
        return kakaoUserInfo.properties?.profileImage
            ?: kakaoUserInfo.kakaoAccount?.profile?.profileImageUrl
    }
}

/**
 * 인증 관련 예외
 */
class AuthException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)