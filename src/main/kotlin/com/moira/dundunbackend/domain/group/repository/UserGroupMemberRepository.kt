package com.moira.dundunbackend.domain.group.repository

import com.moira.dundunbackend.domain.group.dto.response.UserGroupMemberResponse
import com.moira.dundunbackend.domain.group.entity.UserGroupMember
import com.moira.dundunbackend.domain.group.entity.UserGroupMemberId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserGroupMemberRepository : JpaRepository<UserGroupMember, UserGroupMemberId> {
    fun countByUserId(userId: String): Int

    @Query(
        """
        SELECT new com.moira.dundunbackend.domain.group.dto.response.UserGroupMemberResponse(
            GM.groupId,
            GM.userId,
            U.nickname,
            GM.createdAt
        )
        FROM UserGroupMember GM
        INNER JOIN User U ON U.userId  = GM.userId AND U.status = UserStatus.ACTIVE
        WHERE GM.groupId = :groupId
         AND EXISTS (
            SELECT 1 FROM UserGroupMember GM2
            WHERE GM2.groupId = GM.groupId AND GM2.userId = :userId
         )
        ORDER BY GM.createdAt ASC
    """
    )
    fun selectUserGroupMemberResponseList(userId: String, groupId: String): List<UserGroupMemberResponse>
}