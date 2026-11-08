package com.moira.dundunbackend.domain.group.dto.response

import java.time.LocalDateTime

data class UserGroupResponse @JvmOverloads constructor(
    val groupId: String,
    val name: String,
    val memberCount: Int,
    val createdAt: LocalDateTime, // 그룹 생성일
    val recordCount: Int = 0, // TODO
    val lastRecordCreatedAt: LocalDateTime? = null // TODO: 마지막 기록 작성일
)
