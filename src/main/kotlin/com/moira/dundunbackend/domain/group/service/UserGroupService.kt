package com.moira.dundunbackend.domain.group.service

import com.moira.dundunbackend.domain.group.dto.request.UserGroupCreateRequest
import com.moira.dundunbackend.domain.group.dto.request.UserGroupUpdateRequest
import com.moira.dundunbackend.domain.group.dto.response.UserGroupIdResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupMemberResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupResponse
import com.moira.dundunbackend.domain.group.entity.UserGroup
import com.moira.dundunbackend.domain.group.entity.UserGroupMember
import com.moira.dundunbackend.domain.group.repository.UserGroupMemberRepository
import com.moira.dundunbackend.domain.group.repository.UserGroupRepository
import com.moira.dundunbackend.global.common.CommonUtils
import com.moira.dundunbackend.global.common.CommonVariables.GROUP_JOIN_MAX_COUNT
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserGroupService(
    // repository
    private val userGroupRepository: UserGroupRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository
) {
    /**
     * 그룹 생성
     */
    @Transactional
    fun create(userId: String, request: UserGroupCreateRequest): UserGroupIdResponse {
        // 1. UserGroup 저장
        val groupId = CommonUtils.createGroupId()
        val userGroup = UserGroup(groupId, request.name) // memberCount = 1로 default 설정
        userGroupRepository.save(userGroup)

        // 2. 유효성 검사 (사용자는 최대 5개의 그룹에 참여할 수 있다.)
        if (userGroupMemberRepository.countByUserId(userId) >= GROUP_JOIN_MAX_COUNT) {
            throw CommonException(ErrorCode.CAN_JOIN_MAX_5_GROUP)
        }

        // 3. UserGroupMember 저장
        val userGroupMember = UserGroupMember(groupId, userId)
        userGroupMemberRepository.save(userGroupMember)

        // 4. groupId 리턴
        return UserGroupIdResponse(groupId)
    }

    /**
     * 사용자가 참여한 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    fun getAll(userId: String): List<UserGroupResponse> {
        // 1. 조회
        return userGroupRepository.selectUserGroupResponseList(userId)
    }

    /**
     * 그룹에 참여한 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    fun getAllMembers(userId: String, groupId: String): List<UserGroupMemberResponse> {
        // 1. 조회
        val list = userGroupMemberRepository.selectUserGroupMemberResponseList(userId, groupId)

        // 2. 유효성 검사 (list의 size가 0이면 잘못된 접근임.)
        if (list.isEmpty()) {
            throw CommonException(ErrorCode.USER_GROUP_MEMBERS_NOT_FOUND)
        }

        // 3. 리턴
        return list
    }

    /**
     * 그룹 수정
     */
    @Transactional
    fun update(userId: String, groupId: String, request: UserGroupUpdateRequest) {
        // 1. UserGroup 조회
        val userGroup = userGroupRepository.selectUserGroup(userId, groupId)
            ?: throw CommonException(ErrorCode.USER_GROUP_NOT_FOUND)

        // 2. name 수정
        userGroup.name = request.name
    }
}