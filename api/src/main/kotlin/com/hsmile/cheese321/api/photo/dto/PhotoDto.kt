package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema

// ===== 사진 관련 DTO =====

@Schema(description = "사진 업로드 응답")
data class PhotoUploadResponse(
    @Schema(description = "사진 ID")
    val photoId: String,

    @Schema(description = "다운로드 URL")
    val downloadUrl: String
)

@Schema(description = "사진 정보")
data class PhotoResponse(
    @Schema(description = "사진 ID")
    val id: String,

    @Schema(description = "원본 파일명")
    val originalFilename: String,

    @Schema(description = "촬영한 사진관 이름")
    val photoBoothName: String,

    @Schema(description = "업로드 날짜")
    val createdAt: String,

    @Schema(description = "다운로드 URL")
    val downloadUrl: String
)

@Schema(description = "사진 상세 정보")
data class PhotoDetailResponse(
    @Schema(description = "사진 ID")
    val id: String,

    @Schema(description = "원본 파일명")
    val originalFilename: String,

    @Schema(description = "촬영한 사진관 이름")
    val photoBoothName: String,

    @Schema(description = "파일 크기 (bytes)")
    val fileSize: Long,

    @Schema(description = "파일 타입")
    val contentType: String,

    @Schema(description = "업로드 날짜")
    val createdAt: String,

    @Schema(description = "다운로드 URL")
    val downloadUrl: String
)

@Schema(description = "다운로드 URL 응답")
data class PhotoDownloadUrlResponse(
    @Schema(description = "다운로드 URL")
    val downloadUrl: String
)

// ===== 앨범 관련 DTO =====

@Schema(description = "앨범 생성 요청")
data class AlbumCreateRequest(
    @Schema(description = "앨범 이름")
    val name: String,

    @Schema(description = "앨범 설명")
    val description: String? = null
)

@Schema(description = "앨범 정보")
data class AlbumResponse(
    @Schema(description = "앨범 ID")
    val id: String,

    @Schema(description = "앨범 이름")
    val name: String,

    @Schema(description = "앨범 설명")
    val description: String?,

    @Schema(description = "포함된 사진 개수")
    val photoCount: Long,

    @Schema(description = "생성 날짜")
    val createdAt: String
)

@Schema(description = "앨범 상세 정보")
data class AlbumDetailResponse(
    @Schema(description = "앨범 ID")
    val id: String,

    @Schema(description = "앨범 이름")
    val name: String,

    @Schema(description = "앨범 설명")
    val description: String?,

    @Schema(description = "포함된 사진 개수")
    val photoCount: Int,

    @Schema(description = "포함된 사진들")
    val photos: List<PhotoResponse>,

    @Schema(description = "생성 날짜")
    val createdAt: String
)

@Schema(description = "앨범에 사진 추가 요청")
data class AlbumAddPhotosRequest(
    @Schema(description = "추가할 사진 ID들")
    val photoIds: List<String>
)