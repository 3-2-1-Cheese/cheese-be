package com.hsmile.cheese321.api.common.config

import com.hsmile.cheese321.api.common.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정
 * JWT 기반 Stateless 인증 처리
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    /**
     * 개발환경용 Security 설정 - 개발용 토큰 발급 API 허용
     */
    @Bean
    @Profile("local", "dev")
    fun devSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/qr/scan").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            // JWT 필터 활성화
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * 운영환경용 Security 설정 - 엄격한 인증
     */
    @Bean
    @Profile("prod", "staging")
    fun prodSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로 (최소한으로)
                    .requestMatchers("/api/v1/auth/kakao/login").permitAll()
                    .requestMatchers("/api/v1/qr/scan").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            // JWT 필터 활성화
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}