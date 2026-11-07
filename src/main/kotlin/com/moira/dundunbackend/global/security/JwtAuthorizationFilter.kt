package com.moira.dundunbackend.global.security

import com.moira.dundunbackend.global.common.CommonVariables.BEARER
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthorizationFilter(
    private val handlerExceptionResolver: HandlerExceptionResolver,
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {
    private val excludeMatchers = listOf(
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.OPTIONS, "/**"),
        // 로그인
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/api/login/**"),
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login/**"),
        // 토큰 재발급
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/token/reissue")
    )

    /**
     * JwtAuthorizationFilter을 통과하지 않아도 되는 API들의 엔드포인트 패턴을 정의
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return excludeMatchers.any { it.matches(request) }
    }

    /**
     * 토큰 검증
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // 1. 헤더에서 Authorization 헤더 추출
            val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)

            // 2. Bearer 형식 검증
            if (authorizationHeader != null && authorizationHeader.startsWith("$BEARER ")) {
                val token = authorizationHeader.substring(BEARER.length + 1).trim()

                // 3. 토큰 유효성 검증 + userId 추출
                val (userId, role) = jwtProvider.parseUserInfo(token, TokenType.ACCESS_TOKEN).getOrElse {
                    when (it) {
                        is ExpiredJwtException -> throw CommonException(ErrorCode.EXPIRED_ACCESS_TOKEN)
                        else -> throw CommonException(ErrorCode.INVALID_ACCESS_TOKEN)
                    }
                }

                // 4. Authentication 객체 생성 후 SecurityCotnext에 등록
                val authentication = UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${role}")) // role 부여
                )
                SecurityContextHolder.getContext().authentication = authentication
            } else {
                throw CommonException(ErrorCode.INVALID_AUTHORIZATION_HEADER)
            }
        } catch (e: Exception) {
            handlerExceptionResolver.resolveException(request, response, null, e)
            return
        }

        // 6. 다음 필터로 이동
        filterChain.doFilter(request, response)
    }
}