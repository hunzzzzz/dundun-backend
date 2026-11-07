package com.moira.dundunbackend.domain.auth.controller

import com.moira.dundunbackend.domain.auth.dto.request.GoogleLoginRequest
import com.moira.dundunbackend.domain.auth.dto.response.GoogleLoginUrlResponse
import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.auth.service.GoogleLoginService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GoogleLoginController(
    private val googleLoginService: GoogleLoginService
) {
    /**
     * 구글 로그인 URL 전송 (to 프론트엔드)
     */
    @GetMapping("/api/login/google/url")
    fun getGoogleLoginUrl(httpRes: HttpServletResponse): ResponseEntity<GoogleLoginUrlResponse> {
        val response = googleLoginService.getLoginUrl(httpRes)

        return ResponseEntity.ok(response)
    }

    /**
     * 구글 로그인
     */
    @PostMapping("/api/login/google")
    fun googleLogin(
        @RequestBody request: GoogleLoginRequest,
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val response = googleLoginService.googleLogin(request, httpReq, httpRes)

        return ResponseEntity.ok(response)
    }
}