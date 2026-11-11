package com.moira.dundunbackend.domain.user.dto.request

import jakarta.validation.constraints.NotBlank

data class UserImageUpdateRequest(
    @field:NotBlank(message = "NO_USER_IMAGE_FILE_ID")
    val fileId: String
)
