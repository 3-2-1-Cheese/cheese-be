package com.hsmile.cheese321.api.auth.dto

import com.hsmile.cheese321.data.user.entity.User
import io.swagger.v3.oas.annotations.media.Schema

// ===== 요청 DTO =====

@Schema(description = "카카오 로그인 요청")
data class KakaoLoginRequest(
    @Schema(description = "카카오 액세스 토큰")
    val accessToken: String
)

@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @Schema(description = "리프레시 토큰")
    val refreshToken: String
)

// ===== 응답 DTO =====

@Schema(description = "인증 토큰 응답")
data class AuthTokenResponse(
    @Schema(description = "액세스 토큰")
    val accessToken: String,

    @Schema(description = "리프레시 토큰")
    val refreshToken: String,

    @Schema(description = "사용자 정보")
    val user: UserResponse
)

@Schema(description = "사용자 정보 응답")
data class UserResponse(
    @Schema(description = "사용자 ID")
    val id: String,

    @Schema(description = "닉네임")
    val nickname: String,

    @Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String?,

    @Schema(description = "가입일시")
    val createdAt: String
)

// ===== 개발용 DTO =====

@Schema(description = "개발용 토큰 응답")
data class DevTokenResponse(
    @Schema(description = "개발용 액세스 토큰")
    val accessToken: String,

    @Schema(description = "사용자 ID")
    val userId: String,

    @Schema(description = "만료 시간")
    val expiresAt: String,

    @Schema(description = "안내 메시지")
    val message: String
)

@Schema(description = "개발 모드 상태 응답")
data class DevStatusResponse(
    @Schema(description = "개발 모드 활성화 여부")
    val enabled: Boolean,

    @Schema(description = "현재 환경")
    val environment: String,

    @Schema(description = "무효화된 토큰 수")
    val invalidatedTokensCount: Int,

    @Schema(description = "상태 메시지")
    val message: String
)

// TODO: 나중에 구현할 DTO들
// - RefreshTokenResponse (토큰 갱신용 간소화된 응답)
// - 토큰 갱신 시 사용자 정보 전체 반환하지 않고 토큰만 반환

// ===== User Entity 확장 함수 =====

/**
 * User Entity를 UserResponse DTO로 변환
 */
fun User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        nickname = this.nickname,
        profileImageUrl = this.profileImageUrl,
        createdAt = this.createdAt.toString()
    )
}