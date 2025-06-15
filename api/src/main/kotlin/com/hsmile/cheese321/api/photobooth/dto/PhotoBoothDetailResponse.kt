package com.hsmile.cheese321.api.photobooth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진관 상세 응답")
data class PhotoBoothDetailResponse(
    @Schema(description = "사진관 ID")
    val id: String,

    @Schema(description = "사진관 이름")
    val name: String,

    @Schema(description = "브랜드명")
    val brand: String,

    @Schema(description = "지역")
    val region: String,

    @Schema(description = "상세 주소")
    val address: String,

    @Schema(description = "전화번호")
    val phoneNumber: String?,

    @Schema(description = "운영시간", example = "{\"monday\": \"10:00-22:00\", \"tuesday\": \"10:00-22:00\"}")
    val operatingHours: Map<String, String>,

    @Schema(description = "부스 개수")
    val boothCount: Int,

    @Schema(description = "수용 인원")
    val capacity: Int,

    @Schema(description = "평균 평점")
    val rating: Double?,

    @Schema(description = "리뷰 수")
    val reviewCount: Int,

    @Schema(description = "긍정 리뷰 비율")
    val positiveRatio: Double?,

    @Schema(description = "모든 키워드 (사용자 선호 키워드는 하이라이트)")
    val keywords: List<KeywordResponse>,

    @Schema(description = "사진관 이미지 URL들")
    val imageUrls: List<String>,

    @Schema(description = "현재 위치로부터 거리(미터)")
    val distance: Int?
)

@Schema(description = "키워드 응답")
data class KeywordResponse(
    @Schema(description = "키워드명", example = "자연스러운보정")
    val keyword: String,

    @Schema(description = "키워드 타입", example = "사진스타일")
    val type: String,

    @Schema(description = "사용자 선호 키워드 여부")
    val isUserPreferred: Boolean,

    @Schema(description = "관련성 점수", example = "0.85")
    val relevanceScore: Double
)