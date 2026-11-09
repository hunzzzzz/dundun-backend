package com.moira.dundunbackend.domain.group.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserGroupJoinRequest(
    @field:NotBlank(message = "NO_USER_GROUP_INVITE_CODE")
    @field:Size(min = 10, max = 10, message = "INVALID_USER_GROUP_INVITE_CODE_SIZE")
    val inviteCode: String
)
