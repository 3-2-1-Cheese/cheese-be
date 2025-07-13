package com.hsmile.cheese321.api.user.dto

import io.swagger.v3.oas.annotations.media.Schema

// ===== 방문 기록 관련 DTO =====

@Schema(description = "최근 방문 사진관 목록 응답")
data class RecentVisitsResponse(
    @Schema(description = "방문한 사진관 목록")
    val visits: List<VisitHistoryInfo>,

    @Schema(description = "전체 방문 기록 개수")
    val totalCount: Long
)

@Schema(description = "방문 기록 정보")
data class VisitHistoryInfo(
    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "사진관 이름")
    val photoBoothName: String,

    @Schema(description = "브랜드명")
    val brand: String,

    @Schema(description = "지역")
    val region: String,

    @Schema(description = "주소")
    val address: String,

    @Schema(description = "대표 이미지 URL")
    val imageUrl: String?,

    @Schema(description = "마지막 방문 일시")
    val lastVisitedAt: String,

    @Schema(description = "방문 횟수 (현재는 1)")
    val visitCount: Int = 1
)