package com.hsmile.cheese321.data.photo.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 앨범-사진 연결 엔티티
 * 앨범에 포함된 사진들 관리 (N:M 관계)
 */
@Entity
@Table(
    name = "album_photos",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["album_id", "photo_id"])
    ]
)
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

    /**
     * 앨범 내 사진 순서 (나중에 활용)
     */
    @Column(name = "sort_order")
    var sortOrder: Int? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA를 위한 기본 생성자
    protected constructor() : this(
        albumId = "",
        photoId = ""
    )

    /**
     * 순서 업데이트
     */
    fun updateSortOrder(sortOrder: Int) {
        this.sortOrder = sortOrder
    }
}