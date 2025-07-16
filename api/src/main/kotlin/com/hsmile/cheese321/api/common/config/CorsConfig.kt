package com.hsmile.cheese321.api.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * CORS 설정
 */
@Configuration
class CorsConfig {

    @Value("\${app.cors.allowed-origins:http://localhost:3000,http://localhost:8081}")
    private lateinit var allowedOrigins: String

    @Value("\${app.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private lateinit var allowedMethods: String

    @Value("\${app.cors.allowed-headers:*}")
    private lateinit var allowedHeaders: String

    @Value("\${app.cors.allow-credentials:true}")
    private var allowCredentials: Boolean = true

    @Value("\${app.cors.max-age:3600}")
    private var maxAge: Long = 3600

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 허용 Origin 설정
        configuration.allowedOriginPatterns = allowedOrigins.split(",").map { it.trim() }

        // 허용 HTTP 메서드
        configuration.allowedMethods = allowedMethods.split(",").map { it.trim() }

        // 허용 헤더
        if (allowedHeaders == "*") {
            configuration.allowedHeaders = listOf("*")
        } else {
            configuration.allowedHeaders = allowedHeaders.split(",").map { it.trim() }
        }

        // 인증 정보 포함 허용
        configuration.allowCredentials = allowCredentials

        // Preflight 요청 캐시 시간
        configuration.maxAge = maxAge

        // 노출 헤더 (클라이언트 접근 가능 헤더)
        configuration.exposedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Count"
        )

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}