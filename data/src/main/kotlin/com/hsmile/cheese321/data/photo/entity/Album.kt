package com.hsmile.cheese321.data.photo.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 앨범 정보 엔티티
 */
@Entity
@Table(name = "albums")
@EntityListeners(AuditingEntityListener::class)
open class Album(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),

    /**
     * 앨범 소유자 ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,

    /**
     * 앨범 이름
     */
    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    /**
     * 앨범 설명
     */
    @Column(name = "description", length = 500)
    var description: String? = null,

    /**
     * 커버 이미지 URL
     */
    @Column(name = "cover_image_url", length = 500)
    var coverImageUrl: String? = null,

    /**
     * 기본 앨범 여부 (전체 사진 앨범)
     */
    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    // JPA를 위한 기본 생성자
    protected constructor() : this(
        userId = "",
        name = ""
    )

    /**
     * 앨범 정보 업데이트
     */
    fun updateInfo(name: String, description: String?) {
        this.name = name
        this.description = description
    }

    /**
     * 커버 이미지 업데이트
     */
    fun updateCoverImage(coverImageUrl: String?) {
        this.coverImageUrl = coverImageUrl
    }

    companion object {
        /**
         * 기본 앨범 생성 (전체 사진)
         */
        fun createDefaultAlbum(userId: String): Album {
            return Album(
                userId = userId,
                name = "전체 사진",
                description = "모든 사진이 자동으로 저장되는 기본 앨범입니다",
                isDefault = true
            )
        }
    }
}