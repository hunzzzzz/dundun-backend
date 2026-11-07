package com.moira.dundunbackend.domain.user.repository

import com.moira.dundunbackend.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String>