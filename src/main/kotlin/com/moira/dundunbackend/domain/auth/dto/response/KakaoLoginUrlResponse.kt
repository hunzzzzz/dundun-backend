package com.moira.dundunbackend.domain.auth.dto.response

data class KakaoLoginUrlResponse(
    val url: String,
    val state: String
)
