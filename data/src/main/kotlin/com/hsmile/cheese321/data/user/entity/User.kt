package com.hsmile.cheese321.data.user.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 사용자 정보 엔티티
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(
    @Id
    @Column(length = 36)
    val id: String,

    /**
     * 카카오 고유 ID
     */
    @Column(name = "kakao_id", nullable = false, unique = true)
    val kakaoId: Long,

    /**
     * 사용자 닉네임
     */
    @Column(name = "nickname", nullable = false, length = 50)
    var nickname: String,

    /**
     * 프로필 이미지 URL
     */
    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    /**
     * 리프레시 토큰 (로그아웃 시 null로 설정)
     */
    @Column(name = "refresh_token", length = 500)
    var refreshToken: String? = null,

    /**
     * 사용자 선호 키워드 목록 (JSON 배열로 저장)
     * 예: ["자연스러운보정", "빈티지", "화사한톤"]
     */
    @Column(name = "preferred_keywords", columnDefinition = "jsonb")
    var preferredKeywords: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    // JPA를 위한 기본 생성자
    protected constructor() : this(
        id = "",
        kakaoId = 0L,
        nickname = ""
    )

    /**
     * 닉네임 업데이트
     */
    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    /**
     * 리프레시 토큰 업데이트
     */
    fun updateRefreshToken(refreshToken: String?) {
        this.refreshToken = refreshToken
    }

    /**
     * 프로필 이미지 업데이트
     */
    fun updateProfileImage(profileImageUrl: String?) {
        this.profileImageUrl = profileImageUrl
    }

    /**
     * 선호 키워드 업데이트
     */
    fun updatePreferredKeywords(keywordsJson: String?) {
        this.preferredKeywords = keywordsJson
    }
}