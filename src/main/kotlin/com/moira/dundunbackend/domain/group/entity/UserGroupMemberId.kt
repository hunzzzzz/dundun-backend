package com.moira.dundunbackend.domain.group.entity

import java.io.Serializable

data class UserGroupMemberId(
    val groupId: String = "",
    val userId: String = ""
) : Serializable
