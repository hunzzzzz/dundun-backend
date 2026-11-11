package com.moira.dundunbackend.domain.user.dto.response

import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.entity.UserRole
import java.time.LocalDateTime

data class MyPageResponse(
    val userId: String,
    val role: UserRole,
    val oauthProvider: UserOauthProvider,
    val email: String?,
    val nickname: String,
    val fileId: String?,
    var imageUrl: String?,
    val createdAt: LocalDateTime
)
