package com.moira.dundunbackend.domain.auth.dto.request

data class KakaoLoginRequest(
    val code: String,
    val state: String
)
