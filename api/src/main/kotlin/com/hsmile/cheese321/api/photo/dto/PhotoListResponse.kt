package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진 목록 응답")
data class PhotoListResponse(
    @Schema(description = "사진 목록")
    val photos: List<PhotoSummaryResponse>,

    @Schema(description = "페이징 정보")
    val pagination: PaginationResponse,

    @Schema(description = "전체 사진 수")
    val totalCount: Int
)

@Schema(description = "사진 요약 정보")
data class PhotoSummaryResponse(
    @Schema(description = "사진 ID")
    val photoId: String,

    @Schema(description = "사진 이름")
    val photoName: String,

    @Schema(description = "썸네일 URL")
    val thumbnailUrl: String,

    @Schema(description = "저장 날짜")
    val savedAt: String,

    @Schema(description = "사진 타입")
    val photoType: String,

    @Schema(description = "소속 앨범")
    val albums: List<AlbumSummary>? = null
)

@Schema(description = "앨범 요약")
data class AlbumSummary(
    @Schema(description = "앨범 ID")
    val albumId: String,

    @Schema(description = "앨범 이름")
    val albumName: String
)

@Schema(description = "페이징 정보")
data class PaginationResponse(
    @Schema(description = "현재 페이지")
    val currentPage: Int,

    @Schema(description = "페이지 크기")
    val pageSize: Int,

    @Schema(description = "전체 페이지 수")
    val totalPages: Int,

    @Schema(description = "다음 페이지 존재 여부")
    val hasNext: Boolean,

    @Schema(description = "이전 페이지 존재 여부")
    val hasPrevious: Boolean
)