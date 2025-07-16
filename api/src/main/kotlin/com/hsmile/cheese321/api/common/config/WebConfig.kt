package com.hsmile.cheese321.api.common.config

import com.hsmile.cheese321.api.common.interceptor.ApiLoggingInterceptor
import org.springframework.context.annotation.Configuration
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
}