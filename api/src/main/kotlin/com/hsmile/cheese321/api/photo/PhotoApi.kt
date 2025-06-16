package com.hsmile.cheese321.api.photo

import com.hsmile.cheese321.api.photo.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Photo", description = "사진 저장 및 앨범 관리 API")
@RequestMapping(PhotoUris.BASE)
interface PhotoApi {

    @Operation(summary = "사진 저장", description = "QR 스캔된 사진을 갤러리에 저장")
    @PostMapping(PhotoUris.SAVE)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 저장 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 저장 요청"),
            ApiResponse(responseCode = "404", description = "스캔 데이터를 찾을 수 없음"),
            ApiResponse(responseCode = "410", description = "만료된 스캔 데이터")
        ]
    )
    fun savePhotos(@RequestBody request: PhotoSaveRequest): PhotoSaveResponse

    @Operation(summary = "내 사진 목록", description = "사용자가 저장한 모든 사진 조회")
    @GetMapping(PhotoUris.MY_PHOTOS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 목록 조회 성공")
        ]
    )
    fun getMyPhotos(
        @RequestParam userId: String,
        @RequestParam page: Int? = 0,
        @RequestParam size: Int? = 20,
        @RequestParam sortBy: String? = "createdAt"
    ): PhotoListResponse

    @Operation(summary = "사진 상세 조회", description = "특정 사진의 상세 정보")
    @GetMapping(PhotoUris.PHOTO_DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 상세 조회 성공"),
            ApiResponse(responseCode = "404", description = "사진을 찾을 수 없음")
        ]
    )
    fun getPhotoDetail(@PathVariable photoId: String): PhotoDetailResponse

    @Operation(summary = "앨범 생성", description = "새로운 사진 앨범 생성")
    @PostMapping(PhotoUris.ALBUMS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "앨범 생성 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 앨범 정보")
        ]
    )
    fun createAlbum(@RequestBody request: AlbumCreateRequest): AlbumResponse

    @Operation(summary = "내 앨범 목록", description = "사용자의 모든 앨범 조회")
    @GetMapping(PhotoUris.ALBUMS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "앨범 목록 조회 성공")
        ]
    )
    fun getMyAlbums(@RequestParam userId: String): List<AlbumResponse>

    @Operation(summary = "앨범 상세 조회", description = "특정 앨범의 상세 정보")
    @GetMapping(PhotoUris.ALBUM_DETAIL)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "앨범 상세 조회 성공"),
            ApiResponse(responseCode = "404", description = "앨범을 찾을 수 없음")
        ]
    )
    fun getAlbumDetail(@PathVariable albumId: String): AlbumDetailResponse

    @Operation(summary = "앨범에 사진 추가", description = "기존 앨범에 사진 추가")
    @PostMapping(PhotoUris.ALBUM_PHOTOS)
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "사진 추가 성공"),
            ApiResponse(responseCode = "404", description = "앨범 또는 사진을 찾을 수 없음")
        ]
    )
    fun addPhotosToAlbum(
        @PathVariable albumId: String,
        @RequestBody request: AlbumAddPhotosRequest
    ): AlbumDetailResponse
}