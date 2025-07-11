// api/src/main/kotlin/com/hsmile/cheese321/api/auth/dto/KakaoUserInfoResponse.kt

package com.hsmile.cheese321.api.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 카카오 사용자 정보 API 응답 DTO
 * 카카오 API /v2/user/me 응답 구조에 맞춤
 */
data class KakaoUserInfoResponse(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("connected_at")
    val connectedAt: String? = null,

    @JsonProperty("properties")
    val properties: KakaoProperties? = null,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount? = null
)

/**
 * 카카오 사용자 기본 정보
 */
data class KakaoProperties(
    @JsonProperty("nickname")
    val nickname: String? = null,

    @JsonProperty("profile_image")
    val profileImage: String? = null,

    @JsonProperty("thumbnail_image")
    val thumbnailImage: String? = null
)

/**
 * 카카오 계정 정보 (추가 정보)
 */
data class KakaoAccount(
    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean? = null,

    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean? = null,

    @JsonProperty("profile")
    val profile: KakaoProfile? = null
)

/**
 * 카카오 프로필 정보
 */
data class KakaoProfile(
    @JsonProperty("nickname")
    val nickname: String? = null,

    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String? = null,

    @JsonProperty("profile_image_url")
    val profileImageUrl: String? = null,

    @JsonProperty("is_default_image")
    val isDefaultImage: Boolean? = null
)