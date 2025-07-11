package com.hsmile.cheese321.api.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 카카오 로그인 요청 DTO
 */
@Schema(description = "카카오 로그인 요청")
data class KakaoLoginRequest(
    @Schema(description = "카카오 Access Token", example = "xxxxxx")
    val accessToken: String
)

/**
 * 인증 토큰 응답 DTO
 */
@Schema(description = "인증 토큰 응답")
data class AuthTokenResponse(
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String,

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String,

    @Schema(description = "신규 가입 여부", example = "true")
    val isNewUser: Boolean,

    @Schema(description = "사용자 정보")
    val user: UserResponse
)

/**
 * 사용자 정보 응답 DTO
 */
@Schema(description = "사용자 정보")
data class UserResponse(
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,

    @Schema(description = "카카오 ID", example = "12345678")
    val kakaoId: Long,

    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,

    @Schema(description = "프로필 이미지 URL", example = "https://k.kakaocdn.net/...")
    val profileImageUrl: String?
)

/**
 * 토큰 갱신 요청 DTO
 */
@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val refreshToken: String
)

/**
 * 토큰 갱신 응답 DTO
 */
@Schema(description = "토큰 갱신 응답")
data class RefreshTokenResponse(
    @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    val accessToken: String
)

/**
 * User Entity를 UserResponse DTO로 변환하는 확장 함수
 */
fun com.hsmile.cheese321.data.user.entity.User.toUserResponse(): UserResponse {
    return UserResponse(
        id = this.id,
        kakaoId = this.kakaoId,
        nickname = this.nickname,
        profileImageUrl = this.profileImageUrl
    )
}