package com.moira.dundunbackend.domain.group.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserGroupUpdateRequest(
    @field:NotBlank(message = "NO_USER_GROUP_NAME")
    @field:Size(min = 2, max = 20, message = "INVALID_USER_GROUP_NAME_SIZE")
    val name: String
)
