package com.moira.dundunbackend.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoLoginUserAccount(
    @JsonProperty("email")
    val email: String?,

    @JsonProperty("profile")
    val profile: KakaoLoginUserProfile
)
