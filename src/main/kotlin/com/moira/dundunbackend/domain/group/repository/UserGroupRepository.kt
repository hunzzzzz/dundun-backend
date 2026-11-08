package com.moira.dundunbackend.domain.group.repository

import com.moira.dundunbackend.domain.group.dto.response.UserGroupResponse
import com.moira.dundunbackend.domain.group.entity.UserGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserGroupRepository : JpaRepository<UserGroup, String> {
    @Query("""
        SELECT G
        FROM UserGroup G
        INNER JOIN UserGroupMember GM ON GM.groupId = G.groupId AND GM.userId = :userId
        WHERE G.groupId = :groupId
    """)
    fun selectUserGroup(userId: String, groupId: String): UserGroup?

    @Query(
        """
        SELECT new com.moira.dundunbackend.domain.group.dto.response.UserGroupResponse(
            G.groupId,
            G.name,
            G.memberCount,
            G.createdAt
        )
        FROM UserGroup G
        INNER JOIN UserGroupMember GM ON GM.groupId = G.groupId AND GM.userId = :userId
        ORDER BY G.createdAt DESC
    """
    )
    fun selectUserGroupResponseList(userId: String): List<UserGroupResponse>
}