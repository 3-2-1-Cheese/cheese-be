package com.hsmile.cheese321.api.auth.client

import com.hsmile.cheese321.api.auth.dto.KakaoUserInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/**
 * 카카오 API 연동 클라이언트
 * 카카오 서버와 통신하여 사용자 정보를 조회
 */
@Component
class KakaoApiClient {

    private val logger = LoggerFactory.getLogger(KakaoApiClient::class.java)

    private val webClient = WebClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()

    /**
     * 카카오 Access Token을 이용해 사용자 정보 조회
     * @param accessToken 카카오 Access Token
     * @return 카카오 사용자 정보
     * @throws KakaoApiException 카카오 API 호출 실패 시
     */
    fun fetchUserInfo(accessToken: String): KakaoUserInfoResponse {
        return try {
            logger.debug("카카오 사용자 정보 조회 시작")

            val response = webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoUserInfoResponse::class.java)
                .block() ?: throw KakaoApiException("카카오 API 응답이 null입니다")

            logger.debug("카카오 사용자 정보 조회 성공: kakaoId={}", response.id)
            response

        } catch (e: WebClientResponseException) {
            logger.error("카카오 API 호출 실패: status={}, body={}", e.statusCode, e.responseBodyAsString)
            throw KakaoApiException("카카오 사용자 정보 조회 실패: ${e.statusCode}", e)
        } catch (e: Exception) {
            logger.error("카카오 API 호출 중 예외 발생", e)
            throw KakaoApiException("카카오 API 호출 중 오류가 발생했습니다", e)
        }
    }
}

/**
 * 카카오 API 호출 관련 예외
 */
class KakaoApiException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)