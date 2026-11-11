package com.moira.dundunbackend.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserNicknameUpdateRequest(
    @field:NotBlank(message = "NO_USER_NICKNAME")
    @field:Size(min = 2, max = 10, message = "INVALID_USER_NICKNAME_SIZE")
    val newNickname: String
)
