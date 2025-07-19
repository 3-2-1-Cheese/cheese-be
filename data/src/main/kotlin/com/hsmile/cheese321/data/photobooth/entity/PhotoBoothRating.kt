package com.hsmile.cheese321.data.photobooth.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 사진관 별점 엔티티
 */
@Entity
@Table(
    name = "photobooth_ratings",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_user_photobooth_rating",
            columnNames = ["user_id", "photo_booth_id"]
        )
    ],
    indexes = [
        Index(name = "idx_rating_user_id", columnList = "user_id"),
        Index(name = "idx_rating_photo_booth_id", columnList = "photo_booth_id")
    ]
)
@EntityListeners(AuditingEntityListener::class)
class PhotoBoothRating(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 평점을 등록한 사용자 ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,

    /**
     * 평점이 등록된 사진관 ID
     */
    @Column(name = "photo_booth_id", nullable = false, length = 36)
    val photoBoothId: String,

    /**
     * 별점 (1~5점)
     */
    @Column(nullable = false)
    val rating: Int,

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA를 위한 기본 생성자
    protected constructor() : this(
        userId = "",
        photoBoothId = "",
        rating = 1
    )

    init {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
    }

    /**
     * 별점 수정
     */
    fun updateRating(newRating: Int): PhotoBoothRating {
        require(newRating in 1..5) { "Rating must be between 1 and 5" }
        return PhotoBoothRating(
            id = this.id,
            userId = this.userId,
            photoBoothId = this.photoBoothId,
            rating = newRating,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
}