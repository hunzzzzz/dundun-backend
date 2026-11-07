package com.moira.dundunbackend.domain.auth.component

import com.moira.dundunbackend.domain.auth.dto.response.KakaoLoginTokenResponse
import com.moira.dundunbackend.domain.auth.dto.response.KakaoLoginUserResponse
import com.moira.dundunbackend.global.common.CommonVariables.KAKAO_TOKEN_URL
import com.moira.dundunbackend.global.common.CommonVariables.KAKAO_USER_INFO_URL
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
interface KakaoLoginApiClient {
    @PostExchange(value = KAKAO_TOKEN_URL, contentType = "application/x-www-form-urlencoded;charset=utf-8")
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String,
    ): KakaoLoginTokenResponse

    @PostExchange(value = KAKAO_USER_INFO_URL, contentType = "application/x-www-form-urlencoded;charset=utf-8")
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String
    ): KakaoLoginUserResponse
}