package com.moira.dundunbackend.domain.auth.service

import com.moira.dundunbackend.domain.user.entity.User
import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.repository.UserRepository
import com.moira.dundunbackend.global.common.CommonUtils
import com.moira.dundunbackend.global.common.TokenHasher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class LoginTxService(
    private val userRepository: UserRepository
) {
    /**
     * 회원가입 처리
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun signup(
        oauthId: String,
        email: String?,
        nickname: String,
        oauthProvider: UserOauthProvider
    ): User {
        // 1. 닉네임 생성
        var newNickname: String
        do {
            newNickname = CommonUtils.generateRandomNickname(
                oauthProvider = oauthProvider,
                oauthNickname = nickname
            )
        } while (userRepository.existsByNickname(newNickname))

        // 2. User 저장
        val newUser = User.createKakaoUser(
            userId = CommonUtils.createUserId(),
            oauthId = oauthId,
            email = email,
            newNickname = newNickname
        ).let { userRepository.save(it) }

        return newUser
    }

    /**
     * User의 email 변경
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateEmail(userId: String, newEmail: String) {
        // 1. User 재조회
        val user = userRepository.findByIdOrNull(userId)

        // 2. email 변경
        if (user != null) {
            user.email = newEmail
        }
    }

    /**
     * User의 refreshToken 변경
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateRefreshToken(userId: String, refreshToken: String) {
        // 1. User 재조회
        val user = userRepository.findByIdOrNull(userId)

        // 2. refreshToken 변경
        if (user != null) {
            user.refreshToken = TokenHasher.sha256(refreshToken)
        }
    }

    /**
     * User의 refreshToken 초기화
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun nullRefreshToken(userId: String) {
        // 1. User 재조회
        val user = userRepository.findByIdOrNull(userId)

        // 2. refreshToken 초기화
        if (user != null) {
            user.refreshToken = null
        }
    }
}