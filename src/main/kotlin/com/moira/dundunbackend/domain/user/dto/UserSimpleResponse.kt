package com.moira.dundunbackend.domain.user.dto

import com.moira.dundunbackend.domain.user.entity.UserRole

data class UserSimpleResponse(
    val userId: String,
    val role: UserRole,
    val nickname: String,
    val imageUrl: String? = null // TODO
)
