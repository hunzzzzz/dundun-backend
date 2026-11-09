package com.moira.dundunbackend.domain.group.controller

import com.moira.dundunbackend.domain.group.dto.request.UserGroupCreateRequest
import com.moira.dundunbackend.domain.group.dto.request.UserGroupJoinRequest
import com.moira.dundunbackend.domain.group.dto.request.UserGroupUpdateRequest
import com.moira.dundunbackend.domain.group.dto.response.UserGroupIdResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupInviteCodeResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupMemberResponse
import com.moira.dundunbackend.domain.group.dto.response.UserGroupResponse
import com.moira.dundunbackend.domain.group.service.UserGroupService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class UserGroupController(
    private val userGroupService: UserGroupService
) {
    /**
     * 그룹 생성
     */
    @PostMapping("/api/groups")
    fun create(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: UserGroupCreateRequest
    ): ResponseEntity<UserGroupIdResponse> {
        val response = userGroupService.create(userId, request)

        return ResponseEntity.ok(response)
    }

    /**
     * 사용자가 참여한 그룹 목록 조회
     */
    @GetMapping("/api/groups")
    fun getAll(
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<List<UserGroupResponse>> {
        val response = userGroupService.getAll(userId)

        return ResponseEntity.ok(response)
    }

    /**
     * 그룹에 참여한 사용자 목록 조회
     */
    @GetMapping("/api/groups/{groupId}/members")
    fun getAllMembers(
        @AuthenticationPrincipal userId: String,
        @PathVariable groupId: String
    ): ResponseEntity<List<UserGroupMemberResponse>> {
        val response = userGroupService.getAllMembers(userId, groupId)

        return ResponseEntity.ok(response)
    }

    /**
     * 그룹 수정
     */
    @PatchMapping("/api/groups/{groupId}")
    fun update(
        @AuthenticationPrincipal userId: String,
        @PathVariable groupId: String,
        @Valid @RequestBody request: UserGroupUpdateRequest
    ): ResponseEntity<Nothing> {
        userGroupService.update(userId, groupId, request)

        return ResponseEntity.ok(null)
    }

    /**
     * 그룹 탈퇴
     */
    @DeleteMapping("/api/groups/{groupId}")
    fun leave(
        @AuthenticationPrincipal userId: String,
        @PathVariable groupId: String
    ): ResponseEntity<Nothing> {
        userGroupService.leave(userId, groupId)

        return ResponseEntity.ok(null)
    }

    /**
     * 그룹 초대코드 조회
     */
    @GetMapping("/api/groups/{groupId}/invite-code")
    fun getInviteCode(
        @AuthenticationPrincipal userId: String,
        @PathVariable groupId: String
    ): ResponseEntity<UserGroupInviteCodeResponse> {
        val response = userGroupService.getInviteCode(userId, groupId)

        return ResponseEntity.ok(response)
    }

    /**
     * 그룹 참여
     */
    @PostMapping("/api/groups/join")
    fun join(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: UserGroupJoinRequest
    ): ResponseEntity<UserGroupIdResponse> {
        val response = userGroupService.join(userId, request)

        return ResponseEntity.ok(response)
    }
}