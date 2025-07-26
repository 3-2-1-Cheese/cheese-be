package com.hsmile.cheese321.api.common.config

import com.hsmile.cheese321.api.common.interceptor.ApiLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC 설정
 */
@Configuration
class WebConfig(
    private val apiLoggingInterceptor: ApiLoggingInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiLoggingInterceptor)
            .addPathPatterns("/api/**")  // API 경로만 로깅
            .excludePathPatterns(
                "/actuator/**",      // Actuator 제외
                "/swagger-ui/**",    // Swagger UI 제외
                "/api-docs/**"       // API Docs 제외
            )
    }

    /**
     * WebClient 설정 - 추천 서비스 호출과 QR 이미지 다운로드에 공통 사용
     */
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
            .codecs { configurer ->
                // 큰 이미지 파일도 처리할 수 있도록 버퍼 크기 증가
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
            }
    }
}