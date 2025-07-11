package com.hsmile.cheese321.data.user.repository

import com.hsmile.cheese321.data.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사용자 데이터 접근 Repository
 */
@Repository
interface UserRepository : JpaRepository<User, String> {

    /**
     * 카카오 ID로 사용자 조회
     * @param kakaoId 카카오 고유 ID
     * @return 사용자 정보 (없으면 null)
     */
    fun findByKakaoId(kakaoId: Long): User?

    /**
     * 리프레시 토큰으로 사용자 조회
     * @param refreshToken 리프레시 토큰
     * @return 사용자 정보 (없으면 null)
     */
    fun findByRefreshToken(refreshToken: String): User?
}