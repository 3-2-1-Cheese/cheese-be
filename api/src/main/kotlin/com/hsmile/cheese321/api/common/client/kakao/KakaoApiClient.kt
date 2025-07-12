package com.hsmile.cheese321.api.common.client.kakao

import com.hsmile.cheese321.api.common.exception.KakaoApiException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

/**
 * 카카오 API 클라이언트
 */
@Component
class KakaoApiClient(
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://kapi.kakao.com")
        .build()
) {

    /**
     * 카카오 사용자 정보 조회
     */
    fun fetchUserInfo(accessToken: String): KakaoUserInfoResponse {
        return try {
            webClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono<KakaoUserInfoResponse>()
                .block() ?: throw KakaoApiException("카카오 사용자 정보를 가져올 수 없습니다")
        } catch (e: Exception) {
            throw KakaoApiException("카카오 API 호출 중 오류가 발생했습니다: ${e.message}")
        }
    }
}

/**
 * 카카오 API 응답 DTO들
 */
data class KakaoUserInfoResponse(
    val id: Long,
    val properties: KakaoProperties? = null
)

data class KakaoProperties(
    val nickname: String? = null,
    val profile_image: String? = null,
    val thumbnail_image: String? = null
)