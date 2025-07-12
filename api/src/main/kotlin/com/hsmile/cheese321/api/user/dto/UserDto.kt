package com.hsmile.cheese321.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

// ===== 키워드 관련 DTO =====

@Schema(description = "선호 키워드 설정 요청")
data class UpdatePreferredKeywordsRequest(
    @Schema(description = "선호 키워드 목록", example = "[\"자연스러운보정\", \"빈티지\", \"화사한톤\"]")
    @Size(max = 10, message = "키워드는 최대 10개까지 설정할 수 있습니다.")
    val keywords: List<
            @Size(min = 1, max = 20, message = "키워드는 1-20자 사이여야 합니다.")
            String
            >
)

@Schema(description = "선호 키워드 응답")
data class PreferredKeywordsResponse(
    @Schema(description = "선호 키워드 목록")
    val keywords: List<String>
)

@Schema(description = "사용자 프로필 응답")
data class UserProfileResponse(
    @Schema(description = "사용자 ID")
    val id: String,

    @Schema(description = "닉네임")
    val nickname: String,

    @Schema(description = "프로필 이미지 URL")
    val profileImageUrl: String?,

    @Schema(description = "선호 키워드 목록")
    val preferredKeywords: List<String>,

    @Schema(description = "찜한 사진관 개수")
    val favoriteCount: Long
)

// ===== 찜하기 관련 DTO (나중에 사용) =====

@Schema(description = "관심 사진관 목록 응답")
data class FavoritePhotoBoothsResponse(
    @Schema(description = "찜한 사진관 목록")
    val photoBooths: List<FavoritePhotoBoothInfo>
)

@Schema(description = "찜한 사진관 정보")
data class FavoritePhotoBoothInfo(
    @Schema(description = "사진관 ID")
    val id: String,

    @Schema(description = "사진관 이름")
    val name: String,

    @Schema(description = "브랜드명")
    val brand: String,

    @Schema(description = "지역")
    val region: String,

    @Schema(description = "주소")
    val address: String,

    @Schema(description = "대표 이미지 URL")
    val imageUrl: String?,

    @Schema(description = "찜한 날짜")
    val favoritedAt: String
)