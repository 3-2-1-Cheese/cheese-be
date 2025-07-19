package com.hsmile.cheese321.api.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 모든 인증 예외를 하나의 핸들러로 처리
     */
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(
        ex: AuthException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Auth error: {} - {} | Path: {}", ex.errorCode.code, ex.message, request.requestURI)

        // AuthErrorCode에 정의된 httpStatus를 직접 사용
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(ex.errorCode.toErrorResponse(ex.message))
    }

    /**
     * 카카오 API 예외 처리
     */
    @ExceptionHandler(com.hsmile.cheese321.api.common.exception.KakaoApiException::class)
    fun handleLegacyKakaoApiException(e: com.hsmile.cheese321.api.common.exception.KakaoApiException): ResponseEntity<ErrorResponse> {
        logger.warn("Legacy Kakao API error: {}", e.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("KAKAO_API_ERROR", e.message ?: "카카오 API 호출에 실패했습니다."))
    }

    /**
     * Spring Security 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.warn("Spring Security 인증 실패: {}", e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("AUTHENTICATION_FAILED", "인증이 필요합니다."))
    }

    /**
     * Spring Security 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn("접근 권한 없음: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse("ACCESS_DENIED", "접근 권한이 없습니다."))
    }

    /**
     * 유효성 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "요청 데이터가 올바르지 않습니다."
        logger.warn("유효성 검증 실패: {}", errorMessage)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_FAILED", errorMessage))
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("예상치 못한 오류 발생", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."))
    }
}