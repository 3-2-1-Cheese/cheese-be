package com.hsmile.cheese321.data.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 방문 기록 엔티티
 * 사용자가 방문한 사진관 기록 관리 (최근 10개)
 */
@Entity
@Table(
    name = "user_visit_histories",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "photo_booth_id"])
    ],
    indexes = [
        Index(name = "idx_user_visit_user_id", columnList = "user_id"),
        Index(name = "idx_user_visit_visited_at", columnList = "visited_at")
    ]
)
@EntityListeners(AuditingEntityListener::class)
open class UserVisitHistory(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 방문한 사용자 ID
     */
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,

    /**
     * 방문한 사진관 ID
     */
    @Column(name = "photo_booth_id", nullable = false, length = 36)
    val photoBoothId: String,

    /**
     * 방문 일시 (사진 저장 시점)
     */
    @Column(name = "visited_at", nullable = false)
    var visitedAt: LocalDateTime = LocalDateTime.now(),

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA를 위한 기본 생성자
    protected constructor() : this(
        userId = "",
        photoBoothId = ""
    )

    /**
     * 방문 시간 업데이트 (재방문 시 사용)
     */
    fun updateVisitTime() {
        this.visitedAt = LocalDateTime.now()
    }
}