package com.hsmile.cheese321.data.album.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 앨범-사진 연결 엔티티
 * 앨범에 포함된 사진들 관리
 */
@Entity
@Table(name = "album_photos")
@EntityListeners(AuditingEntityListener::class)
class AlbumPhoto(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 앨범 ID
     */
    @Column(name = "album_id", nullable = false)
    val albumId: String,

    /**
     * 사진 ID
     */
    @Column(name = "photo_id", nullable = false)
    val photoId: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    // TODO: 나중에 구현할 기능들
    // - 앨범 내 사진 순서 관리 (sortOrder)
    // - 사진별 캡션 추가
)