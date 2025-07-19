package com.hsmile.cheese321.api.common.client.kakao

import com.hsmile.cheese321.api.common.exception.KakaoApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * 카카오 API 클라이언트
 */
@Component
class KakaoApiClient(
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()
) {

    private val logger = LoggerFactory.getLogger(KakaoApiClient::class.java)

    /**
     * 카카오 사용자 정보 조회
     */
    fun fetchUserInfo(accessToken: String): KakaoUserInfoResponse {
        try {
            logger.debug("Fetching Kakao user info with token")

            val response = webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus({ it.is4xxClientError }) { clientResponse ->
                    when (clientResponse.statusCode().value()) {
                        401 -> {
                            logger.warn("Kakao token expired or invalid")
                            Mono.error(KakaoApiException.tokenExpired())
                        }
                        400 -> {
                            logger.warn("Invalid Kakao token format")
                            Mono.error(KakaoApiException.tokenInvalid())
                        }
                        else -> {
                            logger.error("Kakao API client error: ${clientResponse.statusCode()}")
                            Mono.error(KakaoApiException.apiError())
                        }
                    }
                }
                .onStatus({ it.is5xxServerError }) { clientResponse ->
                    logger.error("Kakao API server error: ${clientResponse.statusCode()}")
                    Mono.error(KakaoApiException.apiError())
                }
                .bodyToMono(KakaoUserInfoResponse::class.java)
                .timeout(Duration.ofSeconds(10))
                .block()

            logger.debug("Successfully fetched Kakao user info: ${response?.id}")
            return response ?: throw KakaoApiException.userInfoFailed()

        } catch (e: KakaoApiException) {
            // 이미 우리가 정의한 예외는 그대로 던지기
            throw e
        } catch (e: WebClientRequestException) {
            logger.error("Network error while calling Kakao API", e)
            throw KakaoApiException.apiError(e)
        } catch (e: TimeoutException) {
            logger.error("Timeout while calling Kakao API", e)
            throw KakaoApiException.apiError(e)
        } catch (e: Exception) {
            logger.error("Unexpected error while calling Kakao API", e)
            throw KakaoApiException.userInfoFailed(e)
        }
    }
}

/**
 * 카카오 사용자 정보 응답 DTO
 */
data class KakaoUserInfoResponse(
    val id: Long,
    val properties: KakaoProperties? = null,
    val kakao_account: KakaoAccount? = null
)

/**
 * 카카오 사용자 속성
 */
data class KakaoProperties(
    val nickname: String? = null
)

/**
 * 카카오 계정 정보
 */
data class KakaoAccount(
    val profile: KakaoProfile? = null
)

/**
 * 카카오 프로필 정보
 */
data class KakaoProfile(
    val nickname: String? = null
)