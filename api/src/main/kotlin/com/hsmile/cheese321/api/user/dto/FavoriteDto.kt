package com.hsmile.cheese321.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema

// ===== 찜하기 관련 DTO =====

@Schema(description = "관심 사진관 목록 응답")
data class FavoritePhotoBoothsResponse(
    @Schema(description = "찜한 사진관 목록")
    val photoBooths: List<FavoritePhotoBoothInfo>,

    @Schema(description = "전체 찜한 사진관 개수")
    val totalCount: Int
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

@Schema(description = "찜하기 토글 응답")
data class FavoriteToggleResponse(
    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "현재 찜 상태")
    val isFavorite: Boolean,

    @Schema(description = "메시지")
    val message: String
)