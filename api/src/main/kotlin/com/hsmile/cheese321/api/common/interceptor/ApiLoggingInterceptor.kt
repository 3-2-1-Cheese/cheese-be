package com.hsmile.cheese321.api.common.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * API 호출 로깅 인터셉터
 */
@Component
class ApiLoggingInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(ApiLoggingInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 시작 시간 기록
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        // 응답 시간 계산
        val startTime = request.getAttribute(START_TIME_ATTR) as? Long ?: return
        val duration = System.currentTimeMillis() - startTime

        // 사용자 정보 추출
        val userId = getCurrentUserId()

        // 요청 정보
        val method = request.method
        val uri = request.requestURI
        val status = response.status
        val clientIp = getClientIp(request)

        // 로그 메시지 구성
        val logMessage = "$method $uri | $status | ${duration}ms | $userId | $clientIp"

        // 로그 레벨 결정
        when {
            ex != null -> logger.error("API_ERROR: $logMessage | Error: ${ex.message}", ex)
            status >= 400 -> logger.warn("API_WARN: $logMessage")
            duration > 3000 -> logger.warn("API_SLOW: $logMessage")
            else -> logger.info("API_INFO: $logMessage")
        }
    }

    /**
     * 현재 로그인한 사용자 ID 추출
     */
    private fun getCurrentUserId(): String {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            when {
                authentication == null -> "Anonymous"
                authentication.name == "anonymousUser" -> "Anonymous"
                else -> authentication.name ?: "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * 클라이언트 IP 추출 (프록시 고려)
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        val xRealIp = request.getHeader("X-Real-IP")

        return when {
            !xForwardedFor.isNullOrBlank() -> xForwardedFor.split(",")[0].trim()
            !xRealIp.isNullOrBlank() -> xRealIp
            else -> request.remoteAddr ?: "Unknown"
        }
    }

    companion object {
        private const val START_TIME_ATTR = "API_START_TIME"
    }
}