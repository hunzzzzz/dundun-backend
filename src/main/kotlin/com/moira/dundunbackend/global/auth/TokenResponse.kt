package com.moira.dundunbackend.global.auth

data class TokenResponse(
    val grantType: String,
    val accessToken: String,
    val accessTokenExpiresTimeMillis: Long
)
