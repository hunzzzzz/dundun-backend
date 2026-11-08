package com.moira.dundunbackend.global.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val jwtAuthorizationFilter: JwtAuthorizationFilter,
    private val loggingFilter: LoggingFilter
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(
                "http://localhost:8080",
                "https://dundun-qa.lovable.app"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }

    @Bean
    fun filterChain(http: HttpSecurity, corsConfigurationSource: CorsConfigurationSource): SecurityFilterChain {
        http {
            // 1. CSRF, 세션, Form 로그인 관련 기능 비활성화
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }

            // 2. JWT 기반 로그인 -> STATELESS
            sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }

            // 3. CORS 처리
            cors { configurationSource = corsConfigurationSource }

            // 4. 예외 처리 (인증되지 않은 요청 시 401 에러 반환 + 인가 오류 시 403 에러 반환)
            exceptionHandling {
                accessDeniedHandler = customAccessDeniedHandler
                authenticationEntryPoint = customAuthenticationEntryPoint
            }

            // 5. 인증/인가 규칙 설정
            authorizeHttpRequests {
                authorize(HttpMethod.OPTIONS, "/**", permitAll)
                // 로그인
                authorize(HttpMethod.GET, "/api/login/**", permitAll)
                authorize(HttpMethod.POST, "/api/login/**", permitAll)
                // 로그아웃
                authorize(HttpMethod.POST, "/api/logout/**", permitAll)
                // 토큰 재발급
                authorize(HttpMethod.POST, "/api/token/reissue", permitAll)
                // 나머지 요청은 인증 필수
                authorize(anyRequest, authenticated)
            }

            // 6. 필터 등록
            addFilterBefore<UsernamePasswordAuthenticationFilter>(loggingFilter)
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthorizationFilter)
        }

        return http.build()
    }
}