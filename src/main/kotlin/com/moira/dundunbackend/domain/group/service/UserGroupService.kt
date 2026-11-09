package com.moira.dundunbackend.domain.group.service

import com.moira.dundunbackend.domain.group.dto.request.UserGroupCreateRequest
import com.moira.dundunbackend.domain.group.dto.request.UserGroupJoinRequest
import com.moira.dundunbackend.domain.group.dto.request.UserGroupUpdateRequest
import com.moira.dundunbackend.domain.group.dto.response.UserGroupIdResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupInviteCodeResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupMemberResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupResponse
import com.moira.dundunbackend.domain.group.entity.UserGroup
import com.moira.dundunbackend.domain.group.entity.UserGroupMember
import com.moira.dundunbackend.domain.group.repository.UserGroupMemberRepository
import com.moira.dundunbackend.domain.group.repository.UserGroupRepository
import com.moira.dundunbackend.global.common.CommonUtils
import com.moira.dundunbackend.global.common.CommonVariables.GROUP_JOIN_MAX_COUNT
import com.moira.dundunbackend.global.common.CommonVariables.GROUP_MEMBERS_MAX_COUNT
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

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
        var inviteCode: String

        do {
            inviteCode = CommonUtils.generateInviteCode()
        } while (userGroupRepository.existsByInviteCode(inviteCode))

        val userGroup = UserGroup(
            groupId = groupId,
            name = request.name,
            inviteCode = inviteCode
        )
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

    /**
     * 그룹 탈퇴
     */
    @Transactional
    fun leave(userId: String, groupId: String) {
        // 1. UserGroup 조회
        val userGroup = userGroupRepository.selectUserGroupForLock(userId, groupId)
            ?: throw CommonException(ErrorCode.USER_GROUP_NOT_FOUND)

        // 2. 현재 멤버 수를 조회
        val memberCount = userGroupMemberRepository.countByGroupId(groupId)

        // 3-1. 탈퇴처리
        if (memberCount > 1) {
            userGroupMemberRepository.deleteByUserIdAndGroupId(userId, groupId)
        }
        // 3-2. 사용자가 유일한 멤버라면 그룹도 삭제처리
        else if (memberCount == 1) {
            // TODO: 기록 관련 엔티티들도 삭제처리
            userGroupMemberRepository.deleteByUserIdAndGroupId(userId, groupId)
            userGroupRepository.delete(userGroup)
        }
        // 3-3. 잘못된 접근
        else {
            throw CommonException(ErrorCode.DELETED_USER_GROUP)
        }
    }

    /**
     * 그룹 초대코드 조회
     */
    @Transactional
    fun getInviteCode(userId: String, groupId: String): UserGroupInviteCodeResponse {
        // 1. UserGroup 조회
        val userGroup = userGroupRepository.selectUserGroupForLock(userId, groupId)
            ?: throw CommonException(ErrorCode.USER_GROUP_NOT_FOUND)

        // 2. 만료된 초대코드인 경우 갱신 필요
        if (userGroup.inviteCodeRefreshDate < LocalDate.now(ZoneId.of("Asia/Seoul"))) {
            var newInviteCode: String

            do {
                newInviteCode = CommonUtils.generateInviteCode()
            } while (userGroupRepository.existsByInviteCode(newInviteCode))

            userGroup.inviteCode = newInviteCode
            userGroup.inviteCodeRefreshDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3)
        }

        // 3. 초대코드 리턴
        return UserGroupInviteCodeResponse(userGroup.inviteCode)
    }

    /**
     * 그룹 참여
     */
    @Transactional
    fun join(userId: String, request: UserGroupJoinRequest): UserGroupIdResponse {
        // 1. UserGroup 조회
        val userGroup = userGroupRepository.selectUserGroupForLockByInviteCode(request.inviteCode)
            ?: throw CommonException(ErrorCode.USER_GROUP_NOT_FOUND_BY_INVITE_CODE)
        val groupId = userGroup.groupId

        // 2. 유효성 검사1
        if (userGroup.inviteCodeRefreshDate < LocalDate.now(ZoneId.of("Asia/Seoul"))) {
            throw CommonException(ErrorCode.EXPIRED_INVITE_CODE)
        }
        if (userGroupMemberRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw CommonException(ErrorCode.ALREADY_JOINED_USER_GROUP)
        }

        // 3. count 조회
        val memberCount = userGroupMemberRepository.countByGroupId(groupId) // 그룹의 멤버 수
        val userJoinCount = userGroupMemberRepository.countByUserId(userId) // 사용자의 그룹 참여 수

        // 4. 유효성 검사2
        if (memberCount >= GROUP_MEMBERS_MAX_COUNT) {
            throw CommonException(ErrorCode.GROUP_MAX_10_MEMBERS)
        }
        if (userJoinCount >= GROUP_JOIN_MAX_COUNT) {
            throw CommonException(ErrorCode.CAN_JOIN_MAX_5_GROUP)
        }

        // 5. UserGroupMember 저장
        val userGroupMember = UserGroupMember(groupId, userId)
        userGroupMemberRepository.save(userGroupMember)

        // 6. groupId 리턴
        return UserGroupIdResponse(groupId)
    }
}