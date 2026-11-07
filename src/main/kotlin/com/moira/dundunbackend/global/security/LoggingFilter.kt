package com.moira.dundunbackend.global.security

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class LoggingFilter : OncePerRequestFilter() {
    private val log = KotlinLogging.logger {}

    /**
     * 로깅
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startTime = System.currentTimeMillis()

        try {
            filterChain.doFilter(request, response)
        } finally {
            val userId = SecurityContextHolder.getContext().authentication?.principal ?: "UNKNOWN"

            log.info {
                """

                ========== 접속 정보 ==========
                [${request.method}] ${request.requestURI}${request.queryString?.let { "?$it" } ?: ""}
                - user: $userId
                - status: ${response.status}
                - duration: ${System.currentTimeMillis() - startTime}ms
                """.trimIndent()
            }
        }
    }
}