package com.hsmile.cheese321.data.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 사용자 엔티티
 * 카카오 소셜 로그인으로 가입한 사용자 정보 관리
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 카카오 고유 ID
     */
    @Column(name = "kakao_id", unique = true, nullable = false)
    val kakaoId: Long,

    /**
     * 사용자 닉네임
     */
    @Column(name = "nickname", nullable = false)
    var nickname: String,

    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null,

    /**
     * 리프레시 토큰 (로그아웃 시 무효화)
     */
    @Column(name = "refresh_token")
    var refreshToken: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    /**
     * 닉네임 업데이트
     */
    fun updateNickname(newNickname: String) {
        this.nickname = newNickname
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 프로필 이미지 업데이트
     */
    fun updateProfileImage(imageUrl: String?) {
        this.profileImageUrl = imageUrl
        this.updatedAt = LocalDateTime.now()
    }

    /**
     * 리프레시 토큰 업데이트
     */
    fun updateRefreshToken(newRefreshToken: String?) {
        this.refreshToken = newRefreshToken
        this.updatedAt = LocalDateTime.now()
    }
}