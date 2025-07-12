package com.hsmile.cheese321.api.photo.spec

import com.hsmile.cheese321.api.photo.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


/**
 * 사진 및 앨범 관리 API
 */
@Tag(name = "Photo & Album", description = "사진 업로드 및 앨범 관리 API")
interface PhotoApi {

    // ===== 사진 관련 API =====

    @Operation(summary = "사진 업로드", description = "사진을 업로드하여 서버에 저장")
    @PostMapping(PhotoUris.BASE + PhotoUris.UPLOAD, consumes = ["multipart/form-data"])
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "업로드 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 파일 형식"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "413", description = "파일 크기 초과")
        ]
    )
    fun uploadPhoto(
        @AuthenticationPrincipal userId: String, // JWT에서 추출
        @RequestParam("photoBoothId") photoBoothId: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<PhotoUploadResponse>

    @Operation(summary = "내 사진 목록", description = "사용자가 업로드한 모든 사진 조회")
    @GetMapping(PhotoUris.BASE + PhotoUris.MY_PHOTOS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getMyPhotos(
        @AuthenticationPrincipal userId: String // JWT에서 추출
    ): ResponseEntity<List<PhotoResponse>>

    @Operation(summary = "사진 상세 조회", description = "특정 사진의 상세 정보 조회")
    @GetMapping(PhotoUris.BASE + PhotoUris.PHOTO_DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음")
        ]
    )
    fun getPhotoDetail(
        @AuthenticationPrincipal userId: String, // 권한 체크용
        @PathVariable photoId: String
    ): ResponseEntity<PhotoDetailResponse>

    @Operation(summary = "사진 다운로드 URL", description = "사진 다운로드용 URL 생성")
    @GetMapping(PhotoUris.BASE + PhotoUris.DOWNLOAD_URL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "URL 생성 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음")
        ]
    )
    fun getDownloadUrl(
        @AuthenticationPrincipal userId: String, // 권한 체크용
        @PathVariable photoId: String
    ): ResponseEntity<PhotoDownloadUrlResponse>

    // ===== 앨범 관련 API =====

    @Operation(summary = "앨범 생성", description = "새로운 사진 앨범 생성")
    @PostMapping(PhotoUris.ALBUMS_BASE + PhotoUris.ALBUMS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "앨범 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun createAlbum(
        @AuthenticationPrincipal userId: String, // JWT에서 추출
        @RequestBody request: AlbumCreateRequest
    ): ResponseEntity<AlbumResponse>

    @Operation(summary = "내 앨범 목록", description = "사용자의 모든 앨범 조회")
    @GetMapping(PhotoUris.ALBUMS_BASE + PhotoUris.MY_ALBUMS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요")
        ]
    )
    fun getMyAlbums(
        @AuthenticationPrincipal userId: String // JWT에서 추출
    ): ResponseEntity<List<AlbumResponse>>

    @Operation(summary = "앨범 상세 조회", description = "앨범 정보와 포함된 사진들 조회")
    @GetMapping(PhotoUris.ALBUMS_BASE + PhotoUris.ALBUM_DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "앨범을 찾을 수 없음")
        ]
    )
    fun getAlbumDetail(
        @AuthenticationPrincipal userId: String, // 권한 체크용
        @PathVariable albumId: String
    ): ResponseEntity<AlbumDetailResponse>

    @Operation(summary = "앨범에 사진 추가", description = "기존 앨범에 사진들 추가")
    @PostMapping(PhotoUris.ALBUMS_BASE + PhotoUris.ALBUM_ADD_PHOTOS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 추가 성공"),
            ApiResponse(responseCode = "401", description = "인증 필요"),
            ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            ApiResponse(responseCode = "404", description = "앨범 또는 사진을 찾을 수 없음"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
        ]
    )
    fun addPhotosToAlbum(
        @AuthenticationPrincipal userId: String, // 권한 체크용
        @PathVariable albumId: String,
        @RequestBody request: AlbumAddPhotosRequest
    ): ResponseEntity<AlbumDetailResponse>

    /**
     * Photo API URI 상수
     */
    object PhotoUris {
        const val BASE = "/api/v1/photos"
        const val UPLOAD = "/upload"
        const val MY_PHOTOS = "/my"
        const val PHOTO_DETAIL = "/{photoId}"
        const val DOWNLOAD_URL = "/{photoId}/download-url"

        const val ALBUMS_BASE = "/api/v1/albums"
        const val ALBUMS = ""
        const val MY_ALBUMS = "/my"
        const val ALBUM_DETAIL = "/{albumId}"
        const val ALBUM_ADD_PHOTOS = "/{albumId}/photos"
    }
}