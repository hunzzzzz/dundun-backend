package com.moira.dundunbackend.domain.group.entity

import com.moira.dundunbackend.domain.base.entity.BaseEntity
import jakarta.persistence.*

@Entity
@IdClass(UserGroupMemberId::class)
@Table(name = "user_group_member", schema = "dundun")
class UserGroupMember(
    @Id
    @Column(name = "group_id", nullable = false)
    var groupId: String,

    @Id
    @Column(name = "user_id", nullable = false)
    var userId: String
) : BaseEntity()