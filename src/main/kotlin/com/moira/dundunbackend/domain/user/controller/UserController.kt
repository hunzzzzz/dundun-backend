package com.moira.dundunbackend.domain.user.controller

import com.moira.dundunbackend.domain.user.dto.request.UserImageUpdateRequest
import com.moira.dundunbackend.domain.user.dto.request.UserNicknameUpdateRequest
import com.moira.dundunbackend.domain.user.dto.response.MyPageResponse
import com.moira.dundunbackend.domain.user.service.UserService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService
) {
    /**
     * 마이페이지 조회
     */
    @GetMapping("/api/me")
    fun myPage(@AuthenticationPrincipal userId: String): ResponseEntity<MyPageResponse> {
        val response = userService.mypage(userId)

        return ResponseEntity.ok(response)
    }

    /**
     * 닉네임 변경
     */
    @PatchMapping("/api/me/nickname")
    fun updateNickname(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: UserNicknameUpdateRequest
    ): ResponseEntity<Nothing> {
        userService.updateNickname(userId, request)

        return ResponseEntity.ok(null)
    }

    /**
     * 프로필 사진 변경
     */
    @PatchMapping("/api/me/image")
    fun updateImage(
        @AuthenticationPrincipal userId: String,
        @Valid @RequestBody request: UserImageUpdateRequest
    ): ResponseEntity<Nothing> {
        userService.updateImage(userId, request)

        return ResponseEntity.ok(null)
    }

    /**
     * 회원탈퇴
     */
    @DeleteMapping("/api/me")
    fun delete(
        @AuthenticationPrincipal userId: String,
        httpRes: HttpServletResponse
    ): ResponseEntity<Nothing> {
        userService.delete(userId, httpRes)

        return ResponseEntity.ok(null)
    }
}