package com.moira.dundunbackend.global.security

data class TokenResponse(
    val grantType: String,
    val accessToken: String,
    val accessTokenExpiresTimeMillis: Long
)
