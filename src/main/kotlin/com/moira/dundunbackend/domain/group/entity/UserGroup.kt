package com.moira.dundunbackend.domain.group.entity

import com.moira.dundunbackend.domain.base.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDate
import java.time.ZoneId

@Entity
@DynamicUpdate
@Table(name = "user_group", schema = "dundun")
class UserGroup(
    @Id
    @Column(name = "group_id", nullable = false, unique = true)
    var groupId: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "invite_code", nullable = false, unique = true)
    var inviteCode: String,

    @Column(name = "invite_code_refresh_date", nullable = false)
    var inviteCodeRefreshDate: LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(3),
) : BaseEntity()