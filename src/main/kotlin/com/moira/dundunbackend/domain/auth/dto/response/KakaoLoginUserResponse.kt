package com.moira.dundunbackend.domain.auth.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoLoginUserResponse(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("kakao_account")
    val account: KakaoLoginUserAccount
)
