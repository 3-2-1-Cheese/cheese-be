package com.hsmile.cheese321.data.photo.repository.dto

/**
 * 앨범별 사진 개수 DTO
 * N+1 문제 해결을 위한 일괄 조회용
 */
data class AlbumPhotoCount(
    val albumId: String,
    val count: Long
)