package com.moira.dundunbackend.domain.auth.dto.response

import com.moira.dundunbackend.domain.user.dto.UserSimpleResponse
import com.moira.dundunbackend.global.auth.TokenResponse

data class LoginResponse(
    val token: TokenResponse?,
    val user: UserSimpleResponse?
)
