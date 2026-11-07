package com.moira.dundunbackend.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoLoginUserProfile(
    @JsonProperty("nickname")
    val nickname: String
)
