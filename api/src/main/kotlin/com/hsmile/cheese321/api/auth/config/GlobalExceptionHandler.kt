package com.hsmile.cheese321.api.config

import com.hsmile.cheese321.api.auth.client.KakaoApiException
import com.hsmile.cheese321.api.auth.service.AuthException
import com.hsmile.cheese321.data.photobooth.exception.PhotoBoothNotFoundException
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
 * API 에러 응답 통일 및 로깅
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 인증 관련 예외 처리
     */
    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<ErrorResponse> {
        logger.warn("인증 오류: {}", e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("AUTH_ERROR", e.message ?: "인증에 실패했습니다"))
    }

    /**
     * 카카오 API 관련 예외 처리
     */
    @ExceptionHandler(KakaoApiException::class)
    fun handleKakaoApiException(e: KakaoApiException): ResponseEntity<ErrorResponse> {
        logger.error("카카오 API 오류: {}", e.message, e)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("KAKAO_API_ERROR", "카카오 로그인 처리 중 오류가 발생했습니다"))
    }

    /**
     * 사진관 조회 관련 예외 처리
     */
    @ExceptionHandler(PhotoBoothNotFoundException::class)
    fun handlePhotoBoothNotFoundException(e: PhotoBoothNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("사진관 조회 오류: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("PHOTOBOOTH_NOT_FOUND", e.message ?: "사진관을 찾을 수 없습니다"))
    }

    /**
     * Spring Security 인증 예외 처리
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.warn("Spring Security 인증 실패: {}", e.message)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse("AUTHENTICATION_FAILED", "인증이 필요합니다"))
    }

    /**
     * Spring Security 권한 예외 처리
     */
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        logger.warn("접근 권한 부족: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse("ACCESS_DENIED", "접근 권한이 없습니다"))
    }

    /**
     * 유효성 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        // 첫 번째 필드 에러 메시지를 대표 메시지로 사용
        val errorMessage = e.bindingResult.fieldErrors.firstOrNull()?.let { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        } ?: "요청 데이터가 올바르지 않습니다"

        logger.warn("유효성 검증 실패: {}", errorMessage)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_FAILED", errorMessage))
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("예상치 못한 오류 발생", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다"))
    }
}

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val code: String,
    val message: String
)