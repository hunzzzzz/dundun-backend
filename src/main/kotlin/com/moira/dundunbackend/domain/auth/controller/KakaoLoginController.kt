package com.moira.dundunbackend.domain.auth.controller

import com.moira.dundunbackend.domain.auth.dto.request.KakaoLoginRequest
import com.moira.dundunbackend.domain.auth.dto.response.KakaoLoginUrlResponse
import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.auth.service.KakaoLoginService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class KakaoLoginController(
    private val kakaoLoginService: KakaoLoginService
) {
    /**
     * 카카오 로그인 URL 전송 (to 프론트엔드)
     */
    @GetMapping("/api/login/kakao/url")
    fun getKakaoLoginUrl(httpRes: HttpServletResponse): ResponseEntity<KakaoLoginUrlResponse> {
        val response = kakaoLoginService.getLoginUrl(httpRes)

        return ResponseEntity.ok(response)
    }

    /**
     * 카카오 로그인
     */
    @PostMapping("/api/login/kakao")
    fun kakaoLogin(
        @RequestBody request: KakaoLoginRequest,
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): ResponseEntity<LoginResponse> {
        val response = kakaoLoginService.kakaoLogin(request, httpReq, httpRes)

        return ResponseEntity.ok(response)
    }
}