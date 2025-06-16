package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "앨범 생성 요청")
data class AlbumCreateRequest(
    @Schema(description = "사용자 ID")
    val userId: String,

    @Schema(description = "앨범 이름")
    val albumName: String,

    @Schema(description = "앨범 설명")
    val description: String? = null,

    @Schema(description = "초기 사진 ID들")
    val initialPhotoIds: List<String>? = null
)

@Schema(description = "앨범에 사진 추가 요청")
data class AlbumAddPhotosRequest(
    @Schema(description = "추가할 사진 ID들")
    val photoIds: List<String>
)