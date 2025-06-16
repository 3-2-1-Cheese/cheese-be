package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사진 저장 응답")
data class PhotoSaveResponse(
    @Schema(description = "저장 결과")
    val result: SaveResult,

    @Schema(description = "저장된 사진들")
    val savedPhotos: List<SavedPhotoInfo>,

    @Schema(description = "생성된 앨범 (앨범 저장 시)")
    val createdAlbum: AlbumResponse? = null,

    @Schema(description = "저장 시간")
    val savedAt: String
)

@Schema(description = "저장된 사진 정보")
data class SavedPhotoInfo(
    @Schema(description = "저장된 사진 ID")
    val photoId: String,

    @Schema(description = "사진 이름")
    val photoName: String,

    @Schema(description = "사진 URL")
    val photoUrl: String,

    @Schema(description = "썸네일 URL")
    val thumbnailUrl: String,

    @Schema(description = "파일 크기")
    val fileSize: Long,

    @Schema(description = "저장 순서")
    val order: Int
)

@Schema(description = "저장 결과")
data class SaveResult(
    @Schema(description = "성공 여부")
    val success: Boolean,

    @Schema(description = "저장된 사진 수")
    val savedCount: Int,

    @Schema(description = "실패한 사진 수")
    val failedCount: Int,

    @Schema(description = "오류 메시지들")
    val errorMessages: List<String>? = null
)