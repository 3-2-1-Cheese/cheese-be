package com.hsmile.cheese321.data.album.repository.dto

/**
 * 앨범별 사진 개수 조회를 위한 DTO
 */
data class AlbumPhotoCountDto(
    val albumId: String,
    val photoCount: Long
)