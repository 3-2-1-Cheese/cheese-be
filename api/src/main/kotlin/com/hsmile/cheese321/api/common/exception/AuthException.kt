package com.hsmile.cheese321.api.common.exception

import org.springframework.http.HttpStatus

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val errorCode: String,
    val message: String
)

/**
 * 인증 관련 에러 코드 정의
 */
enum class AuthErrorCode(val code: String, val message: String, val httpStatus: HttpStatus) {

    // ===== 카카오 로그인 관련 =====
    KAKAO_TOKEN_INVALID("KAKAO_TOKEN_INVALID", "유효하지 않은 카카오 토큰입니다", HttpStatus.BAD_REQUEST),
    KAKAO_TOKEN_EXPIRED("KAKAO_TOKEN_EXPIRED", "카카오 토큰이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    KAKAO_API_ERROR("KAKAO_API_ERROR", "카카오 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_GATEWAY),
    KAKAO_USER_INFO_FAILED("KAKAO_USER_INFO_FAILED", "카카오 사용자 정보를 가져올 수 없습니다", HttpStatus.BAD_GATEWAY),

    // ===== JWT 토큰 관련 =====
    JWT_TOKEN_INVALID("JWT_TOKEN_INVALID", "유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
    JWT_TOKEN_EXPIRED("JWT_TOKEN_EXPIRED", "로그인이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    JWT_TOKEN_MALFORMED("JWT_TOKEN_MALFORMED", "잘못된 형식의 토큰입니다", HttpStatus.BAD_REQUEST),
    JWT_TOKEN_MISSING("JWT_TOKEN_MISSING", "인증이 필요합니다. 로그인해주세요.", HttpStatus.UNAUTHORIZED),

    // ===== Refresh Token 관련 =====
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "유효하지 않은 Refresh Token입니다", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "로그인이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_MISMATCH("REFRESH_TOKEN_MISMATCH", "인증 정보가 일치하지 않습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("REFRESH_TOKEN_NOT_FOUND", "Refresh Token을 찾을 수 없습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),

    // ===== 사용자 관련 =====
    USER_NOT_FOUND("USER_NOT_FOUND", "존재하지 않는 사용자입니다", HttpStatus.NOT_FOUND),
    USER_DEACTIVATED("USER_DEACTIVATED", "비활성화된 계정입니다. 고객센터에 문의해주세요.", HttpStatus.FORBIDDEN),

    // ===== 일반 인증 오류 =====
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "인증에 실패했습니다", HttpStatus.UNAUTHORIZED),
    AUTHORIZATION_FAILED("AUTHORIZATION_FAILED", "권한이 없습니다", HttpStatus.FORBIDDEN),

    // ===== 서버 오류 =====
    TOKEN_GENERATION_FAILED("TOKEN_GENERATION_FAILED", "토큰 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_SERVER_ERROR("AUTH_SERVER_ERROR", "인증 서버 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    fun toErrorResponse(customMessage: String? = null): ErrorResponse {
        return ErrorResponse(
            errorCode = this.code,
            message = customMessage ?: this.message
        )
    }
}

/**
 * 기본 인증 예외
 */
open class AuthException(
    val errorCode: AuthErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message ?: errorCode.message, cause)

/**
 * 카카오 API 관련 예외
 */
class KakaoApiException(
    errorCode: AuthErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : AuthException(errorCode, message, cause) {

    companion object {
        fun tokenExpired() = KakaoApiException(AuthErrorCode.KAKAO_TOKEN_EXPIRED)
        fun tokenInvalid() = KakaoApiException(AuthErrorCode.KAKAO_TOKEN_INVALID)
        fun apiError(cause: Throwable? = null) = KakaoApiException(AuthErrorCode.KAKAO_API_ERROR, cause = cause)
        fun userInfoFailed(cause: Throwable? = null) = KakaoApiException(AuthErrorCode.KAKAO_USER_INFO_FAILED, cause = cause)
    }
}

/**
 * JWT 토큰 관련 예외
 */
class JwtTokenException(
    errorCode: AuthErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : AuthException(errorCode, message, cause) {

    companion object {
        fun expired() = JwtTokenException(AuthErrorCode.JWT_TOKEN_EXPIRED)
        fun invalid() = JwtTokenException(AuthErrorCode.JWT_TOKEN_INVALID)
        fun malformed() = JwtTokenException(AuthErrorCode.JWT_TOKEN_MALFORMED)
        fun missing() = JwtTokenException(AuthErrorCode.JWT_TOKEN_MISSING)
    }
}

/**
 * Refresh Token 관련 예외
 */
class RefreshTokenException(
    errorCode: AuthErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : AuthException(errorCode, message, cause) {

    companion object {
        fun expired() = RefreshTokenException(AuthErrorCode.REFRESH_TOKEN_EXPIRED)
        fun invalid() = RefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID)
        fun mismatch() = RefreshTokenException(AuthErrorCode.REFRESH_TOKEN_MISMATCH)
        fun notFound() = RefreshTokenException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND)
    }
}

/**
 * 사용자 관련 예외
 */
class UserException(
    errorCode: AuthErrorCode,
    message: String? = null,
    cause: Throwable? = null
) : AuthException(errorCode, message, cause) {

    companion object {
        fun notFound(userId: String? = null) = UserException(
            AuthErrorCode.USER_NOT_FOUND,
            "사용자를 찾을 수 없습니다." + if (userId != null) " (ID: $userId)" else ""
        )
        fun deactivated() = UserException(AuthErrorCode.USER_DEACTIVATED)
    }
}