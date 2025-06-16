package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "앨범 응답")
data class AlbumResponse(
    @Schema(description = "앨범 ID")
    val albumId: String,

    @Schema(description = "앨범 이름")
    val albumName: String,

    @Schema(description = "앨범 설명")
    val description: String?,

    @Schema(description = "사진 수")
    val photoCount: Int,

    @Schema(description = "대표 썸네일 URL")
    val thumbnailUrl: String?,

    @Schema(description = "생성 날짜")
    val createdAt: String,

    @Schema(description = "마지막 수정 날짜")
    val updatedAt: String
)

@Schema(description = "앨범 상세 응답")
data class AlbumDetailResponse(
    @Schema(description = "앨범 기본 정보")
    val albumInfo: AlbumResponse,

    @Schema(description = "포함된 사진들")
    val photos: List<PhotoSummaryResponse>
)