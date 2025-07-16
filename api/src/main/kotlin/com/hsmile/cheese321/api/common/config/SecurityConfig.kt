package com.hsmile.cheese321.api.common.config

import com.hsmile.cheese321.api.common.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

/**
 * Spring Security 설정
 * JWT 기반 Stateless 인증 처리
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val corsConfigurationSource: CorsConfigurationSource
) {

    /**
     * 암호 인코더 설정 (Spring Security 표준 구성)
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * 개발환경용 Security 설정 - 개발용 토큰 발급 API 허용
     */
    @Bean
    @Profile("local", "dev")
    fun devSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource)
            }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Preflight 요청 허용
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 인증 없이 접근 가능한 경로
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/qr/scan").permitAll()
                    .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()

                    // 사진관 관련 API (인증 필요)
                    .requestMatchers("/api/v1/photobooths/**").authenticated()

                    // 사용자 관련 API (인증 필요)
                    .requestMatchers("/api/v1/users/**").authenticated()

                    // 사진/앨범 관련 API (인증 필요)
                    .requestMatchers("/api/v1/photos/**").authenticated()
                    .requestMatchers("/api/v1/albums/**").authenticated()

                    // QR 관련 API (일부 인증 필요)
                    .requestMatchers("/api/v1/qr/save-photos").authenticated()

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
            .cors { cors ->
                cors.configurationSource(corsConfigurationSource)
            }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Preflight 요청 허용
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 인증 없이 접근 가능한 경로
                    .requestMatchers("/api/v1/auth/kakao/login").permitAll()
                    .requestMatchers("/api/v1/qr/scan").permitAll()
                    .requestMatchers("/actuator/health").permitAll()

                    // 모든 기능적 API는 인증 필요
                    .requestMatchers("/api/v1/photobooths/**").authenticated()
                    .requestMatchers("/api/v1/users/**").authenticated()
                    .requestMatchers("/api/v1/photos/**").authenticated()
                    .requestMatchers("/api/v1/albums/**").authenticated()
                    .requestMatchers("/api/v1/qr/save-photos").authenticated()

                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            // JWT 필터 활성화
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}