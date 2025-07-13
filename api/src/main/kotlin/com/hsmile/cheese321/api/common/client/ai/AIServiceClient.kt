package com.hsmile.cheese321.api.common.client.ai

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import kotlin.random.Random
import java.time.LocalDateTime

// ===== AI 서비스와 주고받을 DTO들 =====

@Schema(description = "AI 추천 요청용 사용자 정보")
data class UserProfileForAI(
    @Schema(description = "사용자 ID")
    val userId: String,

    @Schema(description = "선호 키워드 목록")
    val preferredKeywords: List<String>,

    @Schema(description = "사용자 현재 위치")
    val location: UserLocation?,

    @Schema(description = "최근 방문한 사진관 ID들")
    val recentVisits: List<String>,

    @Schema(description = "찜한 사진관 ID들")
    val favoritePhotoBooths: List<String>
)

@Schema(description = "사용자 위치 정보")
data class UserLocation(
    @Schema(description = "위도")
    val latitude: Double,

    @Schema(description = "경도")
    val longitude: Double
)

@Schema(description = "AI 추천 결과")
data class AIRecommendationResult(
    @Schema(description = "사진관 ID")
    val photoBoothId: String,

    @Schema(description = "추천 점수 (0.0 ~ 1.0)")
    val score: Double,

    @Schema(description = "추천 이유")
    val reason: String?
)

@Schema(description = "AI 추천 응답")
data class AIRecommendationResponse(
    @Schema(description = "추천 결과 목록")
    val recommendations: List<AIRecommendationResult>,

    @Schema(description = "추천 생성 시간")
    val generatedAt: String,

    @Schema(description = "추천 알고리즘 버전")
    val algorithmVersion: String? = "v1.0"
)

// ===== AI 서비스 클라이언트 =====

/**
 * AI 추천 서비스 클라이언트
 */
@Component
class AIServiceClient(
    @Value("\${app.ai-service.base-url:http://localhost:8081}")
    private val aiServiceBaseUrl: String,

    @Value("\${app.ai-service.timeout:10}")
    private val timeoutSeconds: Long,

    @Value("\${app.ai-service.use-dummy:true}")
    private val useDummy: Boolean
) {

    private val webClient by lazy {
        WebClient.builder()
            .baseUrl(aiServiceBaseUrl)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build()
    }

    /**
     * AI 서비스에서 추천 결과 요청 (코루틴)
     */
    suspend fun getRecommendations(userProfile: UserProfileForAI): AIRecommendationResponse? {
        return if (useDummy) {
            generateDummyRecommendations(userProfile)
        } else {
            try {
                webClient.post()
                    .uri("/api/v1/recommendations")
                    .bodyValue(userProfile)
                    .retrieve()
                    .awaitBody<AIRecommendationResponse>()
            } catch (e: Exception) {
                println("AI 서비스 호출 실패: ${e.message}")
                generateDummyRecommendations(userProfile)
            }
        }
    }

    /**
     * AI 서비스 헬스체크 (코루틴)
     */
    suspend fun healthCheck(): Boolean {
        return if (useDummy) {
            true
        } else {
            try {
                webClient.get()
                    .uri("/health")
                    .retrieve()
                    .awaitBody<Map<String, Any>>()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * 더미 추천 결과 생성
     */
    private fun generateDummyRecommendations(userProfile: UserProfileForAI): AIRecommendationResponse {
        val candidatePhotoBooths = listOf(
            "gangnam-001" to "자연스러운보정",
            "gangnam-002" to "소품다양",
            "gangnam-004" to "화사한톤",
            "hongik-001" to "트렌디한분위기",
            "hongik-003" to "독특한컨셉",
            "jamsil-001" to "친절함",
            "jamsil-003" to "넓은공간",
            "konkuk-004" to "자연스러운보정",
            "konkuk-005" to "깨끗함"
        )

        val recommendations = candidatePhotoBooths.map { (photoBoothId, keyword) ->
            AIRecommendationResult(
                photoBoothId = photoBoothId,
                score = calculateDummyScore(userProfile, keyword),
                reason = "키워드 '${keyword}' 매칭"
            )
        }.sortedByDescending { it.score }.take(8)

        return AIRecommendationResponse(
            recommendations = recommendations,
            generatedAt = LocalDateTime.now().toString(),
            algorithmVersion = "dummy-v1.0"
        )
    }

    /**
     * 더미 점수 계산
     */
    private fun calculateDummyScore(userProfile: UserProfileForAI, photoBoothKeyword: String): Double {
        val baseScore = Random.nextDouble(0.6, 0.9)

        val keywordBonus = if (userProfile.preferredKeywords.any { userKeyword ->
                userKeyword.contains(photoBoothKeyword.substring(0, minOf(2, photoBoothKeyword.length))) ||
                        photoBoothKeyword.contains(userKeyword.substring(0, minOf(2, userKeyword.length)))
            }) 0.15 else 0.0

        val visitBonus = if (userProfile.recentVisits.isNotEmpty()) 0.05 else 0.0
        val favoriteBonus = if (userProfile.favoritePhotoBooths.isNotEmpty()) 0.03 else 0.0

        return (baseScore + keywordBonus + visitBonus + favoriteBonus).coerceAtMost(1.0)
    }
}