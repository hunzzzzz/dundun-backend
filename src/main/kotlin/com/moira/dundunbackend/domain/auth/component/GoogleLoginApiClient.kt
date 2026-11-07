package com.moira.dundunbackend.domain.auth.component

import com.moira.dundunbackend.domain.auth.dto.response.GoogleLoginTokenResponse
import com.moira.dundunbackend.domain.auth.dto.response.GoogleLoginUserResponse
import com.moira.dundunbackend.global.common.CommonVariables.GOOGLE_TOKEN_URL
import com.moira.dundunbackend.global.common.CommonVariables.GOOGLE_USER_INFO_URL
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

@HttpExchange
interface GoogleLoginApiClient {
    @PostExchange(value = GOOGLE_TOKEN_URL, contentType = "application/x-www-form-urlencoded;charset=utf-8")
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("client_secret") clientSecret: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String,
    ): GoogleLoginTokenResponse

    @GetExchange(value = GOOGLE_USER_INFO_URL)
    fun getUserInfo(
        @RequestHeader("Authorization") authorization: String
    ): GoogleLoginUserResponse
}