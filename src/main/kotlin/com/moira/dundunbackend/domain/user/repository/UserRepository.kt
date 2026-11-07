package com.moira.dundunbackend.domain.user.repository

import com.moira.dundunbackend.domain.user.entity.User
import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.entity.UserRole
import com.moira.dundunbackend.domain.user.entity.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun existsByNickname(nickname: String): Boolean
    fun findByOauthIdAndOauthProvider(oauthId: String, oauthProvider: UserOauthProvider): User?
    fun findByUserIdAndRoleAndStatus(userId: String, role: UserRole, status: UserStatus): User?
}