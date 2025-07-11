// api/src/main/kotlin/com/hsmile/cheese321/api/config/SecurityConfig.kt

package com.hsmile.cheese321.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Spring Security 설정
 * JWT 기반 인증 처리
 */
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // 나머지는 인증 필요 (나중에 JWT 필터 추가 예정)
                    .anyRequest().permitAll() // 임시로 모든 요청 허용
            }

        return http.build()
    }
}