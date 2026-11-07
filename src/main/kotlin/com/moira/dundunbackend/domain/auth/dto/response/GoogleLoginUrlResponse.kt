package com.moira.dundunbackend.domain.auth.dto.response

data class GoogleLoginUrlResponse(
    val url: String,
    val state: String
)
