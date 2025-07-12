package com.hsmile.cheese321.api.common.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 * 요청 헤더에서 JWT 토큰을 추출하고 검증하여 Spring Security Context에 인증 정보 설정
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    /**
     * JWT 토큰 검증 및 인증 설정
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 1. 요청에서 JWT 토큰 추출
            val token = extractTokenFromRequest(request)

            // 2. 토큰이 있고 유효한 경우 인증 설정
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // 3. Access Token 또는 개발용 토큰인지 확인
                val tokenType = jwtTokenProvider.getTokenType(token)
                if (tokenType == "access" || tokenType == "dev") {
                    val authentication = jwtTokenProvider.getAuthentication(token)
                    SecurityContextHolder.getContext().authentication = authentication

                    logger.debug("인증 성공: userId=${authentication.name ?: "unknown"}")
                } else {
                    logger.debug("Access Token 또는 개발 토큰이 아닙니다. 인증을 설정하지 않습니다.")
                }
            }

        } catch (e: Exception) {
            logger.error("JWT 인증 처리 중 오류 발생", e)
            // 인증 실패해도 필터 체인은 계속 진행 (Spring Security가 처리)
        }

        // 4. 다음 필터로 요청 전달
        filterChain.doFilter(request, response)
    }

    /**
     * 요청 헤더에서 JWT 토큰 추출
     * @param request HTTP 요청
     * @return JWT 토큰 (Bearer 제외) 또는 null
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)

        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}