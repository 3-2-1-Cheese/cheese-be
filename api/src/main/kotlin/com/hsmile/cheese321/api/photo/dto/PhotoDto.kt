package com.hsmile.cheese321.api.photo.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

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

@Schema(description = "사진 일괄 삭제 요청")
data class PhotoBatchDeleteRequest(
    @Schema(description = "삭제할 사진 ID 목록", example = "[\"photo-1\", \"photo-2\", \"photo-3\"]")
    @field:NotEmpty(message = "삭제할 사진 목록은 비어있을 수 없습니다")
    val photoIds: List<String>
)

@Schema(description = "사진 일괄 삭제 응답")
data class PhotoBatchDeleteResponse(
    @Schema(description = "실제 삭제된 사진 개수", example = "3")
    val deletedCount: Int,

    @Schema(description = "권한이 없어 건너뛴 사진 개수", example = "1")
    val skippedCount: Int,

    @Schema(description = "앨범에서 제거된 총 연결 개수", example = "8")
    val removedFromAlbumsCount: Int,

    @Schema(description = "결과 메시지")
    val message: String
)

@Schema(description = "사진 앨범 이동 요청")
data class PhotoMoveRequest(
    @Schema(description = "이동할 사진 ID 목록", example = "[\"photo-1\", \"photo-2\"]")
    @field:NotEmpty(message = "이동할 사진 목록은 비어있을 수 없습니다")
    val photoIds: List<String>,

    @Schema(description = "목적지 앨범 ID", example = "album-456")
    @field:NotBlank(message = "목적지 앨범 ID는 필수입니다")
    val targetAlbumId: String
)

@Schema(description = "사진 앨범 이동 응답")
data class PhotoMoveResponse(
    @Schema(description = "실제 이동된 사진 개수", example = "2")
    val movedCount: Int,

    @Schema(description = "권한이 없어 건너뛴 사진 개수", example = "0")
    val skippedCount: Int,

    @Schema(description = "이미 앨범에 있어서 건너뛴 사진 개수", example = "1")
    val alreadyInAlbumCount: Int,

    @Schema(description = "결과 메시지")
    val message: String
)

@Schema(description = "앨범에서 사진 일괄 제거 요청")
data class AlbumPhotoBatchRemoveRequest(
    @Schema(description = "제거할 사진 ID 목록", example = "[\"photo-1\", \"photo-2\"]")
    @field:NotEmpty(message = "제거할 사진 목록은 비어있을 수 없습니다")
    val photoIds: List<String>
)

@Schema(description = "앨범에서 사진 제거 응답")
data class AlbumPhotoRemoveResponse(
    @Schema(description = "실제 제거된 사진 개수", example = "2")
    val removedCount: Int,

    @Schema(description = "앨범에 없어서 건너뛴 사진 개수", example = "1")
    val notFoundCount: Int,

    @Schema(description = "결과 메시지")
    val message: String
)

// ===== 앨범 관련 DTO =====

@Schema(description = "앨범 생성 요청")
data class AlbumCreateRequest(
    @Schema(description = "앨범 이름")
    @field:NotBlank(message = "앨범 이름은 필수입니다")
    @field:Size(max = 50, message = "앨범 이름은 50자 이하여야 합니다")
    val name: String,

    @Schema(description = "앨범 설명")
    @field:Size(max = 200, message = "앨범 설명은 200자 이하여야 합니다")
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

    @Schema(description = "커버 이미지 URL")
    val coverImageUrl: String?,

    @Schema(description = "생성 날짜")
    val createdAt: String,

    @Schema(description = "기본 앨범 여부")
    val isDefault: Boolean = false
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

    @Schema(description = "커버 이미지 URL")
    val coverImageUrl: String?,

    @Schema(description = "생성 날짜")
    val createdAt: String,

    @Schema(description = "기본 앨범 여부")
    val isDefault: Boolean = false
)

@Schema(description = "앨범에 사진 추가 요청")
data class AlbumAddPhotosRequest(
    @Schema(description = "추가할 사진 ID들")
    val photoIds: List<String>
)

@Schema(description = "앨범 수정 요청")
data class AlbumUpdateRequest(
    @Schema(description = "앨범 이름", example = "강남역 추억 모음")
    @field:NotBlank(message = "앨범 이름은 필수입니다")
    @field:Size(max = 50, message = "앨범 이름은 50자 이하여야 합니다")
    val name: String,

    @Schema(description = "앨범 설명", example = "친구들과 함께한 소중한 순간들")
    @field:Size(max = 200, message = "앨범 설명은 200자 이하여야 합니다")
    val description: String?
)

@Schema(description = "앨범에서 사진 제거 요청")
data class AlbumRemovePhotosRequest(
    @Schema(description = "제거할 사진 ID 목록")
    val photoIds: List<String>
)

@Schema(description = "앨범 삭제 응답")
data class AlbumDeleteResponse(
    @Schema(description = "삭제된 앨범 ID")
    val albumId: String,

    @Schema(description = "메시지")
    val message: String,

    @Schema(description = "삭제된 사진 개수")
    val deletedPhotoCount: Int
)