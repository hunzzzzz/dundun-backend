package com.moira.dundunbackend.domain.group.dto.response

import java.time.LocalDateTime

data class UserGroupMemberResponse @JvmOverloads constructor(
    val groupId: String,
    val userId: String,
    val nickname: String,
    val joinedAt: LocalDateTime, // 그룹 참여일시
    val recordCount: Int = 0, // 해당 멤버가 작성한 기록 수
)
