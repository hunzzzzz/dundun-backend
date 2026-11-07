package com.moira.dundunbackend.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoLoginTokenResponse(
    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("id_token")
    val idToken: String?,

    @JsonProperty("scope")
    val scope: String?,
)
