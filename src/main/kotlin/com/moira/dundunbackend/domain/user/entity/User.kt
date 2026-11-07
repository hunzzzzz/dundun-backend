package com.moira.dundunbackend.domain.user.entity

import com.moira.dundunbackend.domain.base.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime

@Entity
@DynamicUpdate
@Table(name = "user", schema = "dundun")
class User(
    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    var userId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: UserStatus = UserStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: UserRole = UserRole.USER,

    @Column(name = "oauth_id", nullable = false)
    var oauthId: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    var oauthProvider: UserOauthProvider,

    @Column(name = "email", nullable = true)
    var email: String?,

    @Column(name = "nickname", nullable = false)
    var nickname: String,

    @Column(name = "file_id", nullable = true)
    var fileId: String? = null,

    @Column(name = "deleted_at", nullable = true)
    var deletedAt: LocalDateTime? = null,

    @Column(name = "refresh_token", nullable = true)
    var refreshToken: String? = null,
) : BaseEntity()