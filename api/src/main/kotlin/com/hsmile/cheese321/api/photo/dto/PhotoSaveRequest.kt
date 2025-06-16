package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진 저장 요청")
data class PhotoSaveRequest(
    @Schema(description = "QR 스캔 ID")
    val scanId: String,

    @Schema(description = "사용자 ID")
    val userId: String,

    @Schema(description = "저장 옵션")
    val saveOptions: PhotoSaveOptions,

    @Schema(description = "앨범 정보 (앨범 저장 시)")
    val albumInfo: AlbumInfo? = null
)

@Schema(description = "사진 저장 옵션")
data class PhotoSaveOptions(
    @Schema(description = "저장 타입", example = "INDIVIDUAL")
    val saveType: PhotoSaveType,

    @Schema(description = "저장할 개별 사진 ID들 (개별 저장 시)")
    val selectedPhotoIds: List<String>? = null,

    @Schema(description = "저장 형식", example = "JPEG")
    val format: String? = "JPEG",

    @Schema(description = "이미지 품질", example = "HIGH")
    val quality: ImageQuality? = ImageQuality.HIGH
)

@Schema(description = "앨범 정보")
data class AlbumInfo(
    @Schema(description = "앨범 이름")
    val albumName: String,

    @Schema(description = "앨범 설명")
    val description: String? = null,

    @Schema(description = "기존 앨범 ID (기존 앨범에 추가 시)")
    val existingAlbumId: String? = null
)

@Schema(description = "사진 저장 타입")
enum class PhotoSaveType {
    @Schema(description = "전체 사진 저장")
    ORIGINAL,

    @Schema(description = "개별 사진 저장")
    INDIVIDUAL,

    @Schema(description = "앨범으로 저장")
    ALBUM
}

@Schema(description = "이미지 품질")
enum class ImageQuality {
    @Schema(description = "높은 품질")
    HIGH,

    @Schema(description = "중간 품질")
    MEDIUM,

    @Schema(description = "낮은 품질")
    LOW
}