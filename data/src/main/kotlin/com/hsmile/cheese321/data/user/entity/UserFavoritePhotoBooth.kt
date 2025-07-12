package com.hsmile.cheese321.data.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.UUID

/**
 * 사용자 관심 사진관 엔티티
 * 사용자가 찜한 사진관 정보 저장
 */
@Entity
@Table(
    name = "user_favorite_photobooths",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_photobooth",
            columnNames = ["user_id", "photo_booth_id"]
        )
    ]
)
@EntityListeners(AuditingEntityListener::class)
class UserFavoritePhotoBooth(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,

    /**
     * 사진관 ID
     */
    @Column(name = "photo_booth_id", nullable = false, length = 36)
    val photoBoothId: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    // JPA를 위한 기본 생성자
    protected constructor() : this(
        userId = "",
        photoBoothId = ""
    )
}