package com.hsmile.cheese321.api.qr.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진 미리보기 응답")
data class PhotoPreviewResponse(
    @Schema(description = "스캔 ID")
    val scanId: String,

    @Schema(description = "원본 사진 미리보기 URL")
    val previewUrl: String,

    @Schema(description = "사진 정보")
    val photoInfo: PhotoInfoResponse,

    @Schema(description = "분할된 개별 사진들 (인생네컷인 경우)")
    val individualPhotos: List<IndividualPhotoResponse>? = null,

    @Schema(description = "저장 가능한 옵션들")
    val saveOptions: SaveOptionsResponse,

    @Schema(description = "만료 시간")
    val expiresAt: String
)

@Schema(description = "개별 사진 정보")
data class IndividualPhotoResponse(
    @Schema(description = "개별 사진 ID")
    val photoId: String,

    @Schema(description = "개별 사진 미리보기 URL")
    val previewUrl: String,

    @Schema(description = "순서", example = "1")
    val order: Int,

    @Schema(description = "크롭 좌표")
    val cropInfo: CropInfoResponse
)

@Schema(description = "크롭 정보")
data class CropInfoResponse(
    @Schema(description = "시작 X 좌표")
    val x: Int,

    @Schema(description = "시작 Y 좌표")
    val y: Int,

    @Schema(description = "너비")
    val width: Int,

    @Schema(description = "높이")
    val height: Int
)

@Schema(description = "저장 옵션")
data class SaveOptionsResponse(
    @Schema(description = "전체 사진 저장 가능 여부")
    val canSaveOriginal: Boolean,

    @Schema(description = "개별 사진 저장 가능 여부")
    val canSaveIndividual: Boolean,

    @Schema(description = "앨범 생성 가능 여부")
    val canCreateAlbum: Boolean,

    @Schema(description = "지원하는 저장 형식들")
    val supportedFormats: List<String>
)