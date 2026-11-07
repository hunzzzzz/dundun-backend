package com.moira.dundunbackend.domain.auth.controller

import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val authService: AuthService
) {
    /**
     * 로그아웃
     */
    @PostMapping("/api/logout")
    fun logout(
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): ResponseEntity<Nothing> {
        authService.logout(httpReq, httpRes)

        return ResponseEntity.ok(null)
    }

    /**
     * 토큰 재발급
     */
    @PostMapping("/api/token/reissue")
    fun reissueToken(
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val response = authService.reissueToken(httpReq, httpRes)

        return ResponseEntity.ok(response)
    }
}