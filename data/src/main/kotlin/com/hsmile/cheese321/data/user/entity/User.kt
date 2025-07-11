package com.hsmile.cheese321.data.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 엔티티
 * 카카오 소셜 로그인 기반 사용자 정보 관리
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 카카오 고유 ID (유니크)
     * 카카오에서 제공하는 사용자 식별자
     */
    @Column(name = "kakao_id", unique = true, nullable = false)
    val kakaoId: Long,

    /**
     * 사용자 닉네임
     * 카카오 프로필 정보로 업데이트 가능
     */
    @Column(nullable = false)
    var nickname: String,

    /**
     * 프로필 이미지 URL
     * 카카오 프로필 이미지로 업데이트 가능
     */
    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    /**
     * 리프레시 토큰
     * JWT 갱신용 토큰 저장
     */
    @Column(name = "refresh_token", length = 500)
    var refreshToken: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 사용자 정보 업데이트
     * 카카오 로그인 시 최신 정보로 갱신
     */
    fun updateProfile(nickname: String, profileImageUrl: String?) {
        this.nickname = nickname
        this.profileImageUrl = profileImageUrl
    }

    /**
     * 리프레시 토큰 업데이트
     */
    fun updateRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}