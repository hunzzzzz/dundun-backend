package com.moira.dundunbackend.domain.user.repository

import com.moira.dundunbackend.domain.user.dto.response.MyPageResponse
import com.moira.dundunbackend.domain.user.entity.User
import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.entity.UserRole
import com.moira.dundunbackend.domain.user.entity.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun existsByNickname(nickname: String): Boolean
    fun findByOauthIdAndOauthProvider(oauthId: String, oauthProvider: UserOauthProvider): User?
    fun findByUserIdAndStatus(userId: String, status: UserStatus): User?
    fun findByUserIdAndRoleAndStatus(userId: String, role: UserRole, status: UserStatus): User?

    @Query(
        """
        SELECT new com.moira.dundunbackend.domain.user.dto.response.MyPageResponse(
            U.userId,
            U.role,
            U.oauthProvider,
            U.email,
            U.nickname,
            U.fileId,
            NULL AS imageUrl,
            U.createdAt
        )
        FROM User U
        LEFT JOIN ImageFile F ON F.fileId = U.fileId
        WHERE U.userId = :userId
         AND  U.status = UserStatus.ACTIVE
    """
    )
    fun selectMyPageResponse(userId: String): MyPageResponse?

    @Query(
        """
            SELECT U
            FROM User U
            WHERE U.status = UserStatus.DELETED
             AND  U.deletedAt <= :dayBefore60Days
        """
    )
    fun selectAllForDelete(dayBefore60Days: LocalDateTime = LocalDateTime.now().minusDays(60)): List<User>

}