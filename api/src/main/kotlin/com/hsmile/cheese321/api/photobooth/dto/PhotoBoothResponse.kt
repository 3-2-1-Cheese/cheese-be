package com.hsmile.cheese321.api.photobooth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진관 목록 응답")
data class PhotoBoothResponse(
    @Schema(description = "사진관 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,

    @Schema(description = "사진관 이름", example = "인생네컷 강남역1호점")
    val name: String,

    @Schema(description = "브랜드명", example = "인생네컷")
    val brand: String,

    @Schema(description = "지역", example = "강남역")
    val region: String,

    @Schema(description = "주소", example = "서울시 강남구 역삼동 123-45")
    val address: String,

    @Schema(description = "리뷰 수", example = "127")
    val reviewCount: Int,

    @Schema(description = "현재 위치로부터 거리(미터)", example = "450")
    val distance: Int,

    @Schema(description = "대표 이미지 URL", example = "https://example.com/photo.jpg")
    val imageUrl: String?,

    @Schema(description = "사용자 맞춤 추천 여부 (선호 키워드 매칭 시 true)")
    val isRecommended: Boolean,

    @Schema(description = "사용자가 찜한 사진관 여부")
    val isFavorite: Boolean
)