package com.moira.dundunbackend.domain.group.entity

import com.moira.dundunbackend.domain.base.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.DynamicUpdate

@Entity
@DynamicUpdate
@Table(name = "user_group", schema = "dundun")
class UserGroup(
    @Id
    @Column(name = "group_id", nullable = false, unique = true)
    var groupId: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "member_count", nullable = false)
    var memberCount: Int = 1
) : BaseEntity()