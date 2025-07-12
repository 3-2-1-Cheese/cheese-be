package com.hsmile.cheese321.api.common.security

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성 및 검증 관리
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())

    /**
     * Access Token 생성
     */
    fun generateAccessToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenValidity)

        return Jwts.builder()
            .setSubject(userId)
            .claim("type", "access")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenValidity)

        return Jwts.builder()
            .setSubject(userId)
            .claim("type", "refresh")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * 개발용 무제한 토큰 생성 (로컬 환경 전용)
     */
    fun generateDevToken(userId: String = "dev-user-123"): String {
        val now = Date()
        // 100년 후 만료 (사실상 무제한)
        val expiryDate = Date(now.time + (100L * 365 * 24 * 60 * 60 * 1000))

        return Jwts.builder()
            .setSubject(userId)
            .claim("type", "dev")
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return claims.subject
    }

    /**
     * 토큰 타입 확인
     */
    fun getTokenType(token: String): String {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return claims.get("type", String::class.java) ?: "unknown"
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (e: SecurityException) {
            logger.debug("잘못된 JWT 서명입니다.")
            false
        } catch (e: MalformedJwtException) {
            logger.debug("잘못된 JWT 서명입니다.")
            false
        } catch (e: ExpiredJwtException) {
            logger.debug("만료된 JWT 토큰입니다.")
            false
        } catch (e: UnsupportedJwtException) {
            logger.debug("지원되지 않는 JWT 토큰입니다.")
            false
        } catch (e: IllegalArgumentException) {
            logger.debug("JWT 토큰이 잘못되었습니다.")
            false
        }
    }

    /**
     * 토큰에서 인증 정보 조회
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

        return UsernamePasswordAuthenticationToken(claims.subject, null, authorities)
    }
}