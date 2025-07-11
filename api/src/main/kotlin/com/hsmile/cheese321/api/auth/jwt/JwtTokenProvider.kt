package com.hsmile.cheese321.api.auth.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 * Access Token과 Refresh Token 관리
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,

    @Value("\${jwt.access-token-validity}")
    private val accessTokenValidityInMs: Long,

    @Value("\${jwt.refresh-token-validity}")
    private val refreshTokenValidityInMs: Long
) {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Access Token 생성
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    fun generateAccessToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenValidityInMs)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "access")
            .signWith(key) // 알고리즘 자동 선택
            .compact()
    }

    /**
     * Refresh Token 생성
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    fun generateRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenValidityInMs)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "refresh")
            .signWith(key) // 알고리즘 자동 선택
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.subject
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: SecurityException) {
            logger.error("잘못된 JWT 서명입니다: {}", e.message)
            false
        } catch (e: MalformedJwtException) {
            logger.error("잘못된 JWT 서명입니다: {}", e.message)
            false
        } catch (e: ExpiredJwtException) {
            logger.error("만료된 JWT 토큰입니다: {}", e.message)
            false
        } catch (e: UnsupportedJwtException) {
            logger.error("지원되지 않는 JWT 토큰입니다: {}", e.message)
            false
        } catch (e: IllegalArgumentException) {
            logger.error("JWT 토큰이 잘못되었습니다: {}", e.message)
            false
        }
    }

    /**
     * 토큰에서 Spring Security Authentication 객체 생성
     * @param token JWT 토큰
     * @return Authentication 객체
     */
    fun getAuthentication(token: String): Authentication {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        return UsernamePasswordAuthenticationToken(claims.subject, null, authorities)
    }

    /**
     * 토큰 타입 확인 (access/refresh)
     * @param token JWT 토큰
     * @return 토큰 타입
     */
    fun getTokenType(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims["type"] as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 토큰 만료 시간 확인
     * @param token JWT 토큰
     * @return 만료 시간
     */
    fun getExpirationDateFromToken(token: String): Date? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.expiration
        } catch (e: Exception) {
            null
        }
    }
}