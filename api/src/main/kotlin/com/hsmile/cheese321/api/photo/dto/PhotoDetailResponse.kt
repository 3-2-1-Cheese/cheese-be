package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진 상세 응답")
data class PhotoDetailResponse(
    @Schema(description = "사진 ID")
    val photoId: String,

    @Schema(description = "사진 이름")
    val photoName: String,

    @Schema(description = "원본 사진 URL")
    val photoUrl: String,

    @Schema(description = "썸네일 URL")
    val thumbnailUrl: String,

    @Schema(description = "사진 메타데이터")
    val metadata: PhotoMetadata,

    @Schema(description = "저장 정보")
    val saveInfo: PhotoSaveInfo,

    @Schema(description = "소속 앨범들")
    val albums: List<AlbumSummary>
)

@Schema(description = "사진 메타데이터")
data class PhotoMetadata(
    @Schema(description = "파일 크기")
    val fileSize: Long,

    @Schema(description = "이미지 너비")
    val width: Int,

    @Schema(description = "이미지 높이")
    val height: Int,

    @Schema(description = "파일 형식")
    val format: String,

    @Schema(description = "촬영 위치 (추정)")
    val location: String? = null,

    @Schema(description = "촬영 날짜 (추정)")
    val takenAt: String? = null
)

@Schema(description = "사진 저장 정보")
data class PhotoSaveInfo(
    @Schema(description = "저장 날짜")
    val savedAt: String,

    @Schema(description = "저장 타입")
    val saveType: PhotoSaveType,

    @Schema(description = "원본 QR 스캔 ID")
    val originalScanId: String,

    @Schema(description = "저장 품질")
    val quality: ImageQuality
)