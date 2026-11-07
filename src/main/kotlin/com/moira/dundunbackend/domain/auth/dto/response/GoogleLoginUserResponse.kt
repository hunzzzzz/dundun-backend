package com.moira.dundunbackend.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleLoginUserResponse(
    @JsonProperty("sub")
    val sub: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("email")
    val email: String?,

    @JsonProperty("email_verified")
    val emailVerified: Boolean?
)
