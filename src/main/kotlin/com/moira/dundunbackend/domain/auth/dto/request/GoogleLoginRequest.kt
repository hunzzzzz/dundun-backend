package com.moira.dundunbackend.domain.auth.dto.request

data class GoogleLoginRequest(
    val code: String,
    val state: String
)
