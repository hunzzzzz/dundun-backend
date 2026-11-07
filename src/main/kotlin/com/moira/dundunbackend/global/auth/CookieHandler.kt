package com.moira.dundunbackend.global.auth

import com.moira.dundunbackend.global.common.CommonVariables.LOCAL
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component

@Component
class CookieHandler(
    private val environment: Environment
) {
    private val isLocal: Boolean by lazy { environment.activeProfiles.contains(LOCAL) }

    /**
     * [내부 함수] 쿠키 빌드
     */
    private fun build(cookieName: String, value: String, maxAgeSec: Long): ResponseCookie =
        ResponseCookie.from(cookieName, value)
            .httpOnly(true)
            .secure(!isLocal)                         // 로컬: false, 운영: true
            .sameSite(if (isLocal) "Lax" else "None") // 로컬: Lax, 운영: None
            .path("/")
            .maxAge(maxAgeSec)
            .build()

    /**
     * 쿠키에 특정 값 저장
     */
    fun add(httpRes: HttpServletResponse, cookieName: String, value: String, expireSec: Int) {
        httpRes.addHeader(HttpHeaders.SET_COOKIE, build(cookieName, value, expireSec.toLong()).toString())
    }

    /**
     * 쿠키에서 특정 값 추출
     */
    fun get(httpReq: HttpServletRequest, cookieName: String): String? {
        return httpReq.cookies?.find { it.name == cookieName }?.value
    }

    /**
     * 쿠키 만료 처리
     */
    fun expire(httpRes: HttpServletResponse, cookieName: String) {
        httpRes.addHeader(HttpHeaders.SET_COOKIE, build(cookieName, "", 0).toString())
    }
}