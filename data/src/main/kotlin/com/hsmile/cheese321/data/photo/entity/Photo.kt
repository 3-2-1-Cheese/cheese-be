package com.hsmile.cheese321.data.photo.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 사진 엔티티
 * 사용자가 업로드한 사진 정보 관리
 */
@Entity
@Table(name = "photos")
@EntityListeners(AuditingEntityListener::class)
open class Photo(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 사진 업로드한 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    val userId: String,

    /**
     * 촬영한 사진관 ID
     */
    @Column(name = "photo_booth_id", nullable = false)
    val photoBoothId: String,

    /**
     * 파일 저장 경로 (S3 key 등)
     */
    @Column(name = "file_path", nullable = false)
    val filePath: String,

    /**
     * 원본 파일명
     */
    @Column(name = "original_filename", nullable = false)
    val originalFilename: String,

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size", nullable = false)
    val fileSize: Long,

    /**
     * 파일 MIME 타입 (image/jpeg, image/png 등)
     */
    @Column(name = "content_type", nullable = false)
    val contentType: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA를 위한 기본 생성자
    protected constructor() : this(
        userId = "",
        photoBoothId = "",
        filePath = "",
        originalFilename = "",
        fileSize = 0L,
        contentType = ""
    )
}