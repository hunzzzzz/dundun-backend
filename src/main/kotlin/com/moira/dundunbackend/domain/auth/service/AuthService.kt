package com.moira.dundunbackend.domain.auth.service

import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.user.entity.UserRole
import com.moira.dundunbackend.domain.user.entity.UserStatus
import com.moira.dundunbackend.domain.user.repository.UserRepository
import com.moira.dundunbackend.domain.user.service.UserService
import com.moira.dundunbackend.global.auth.CookieHandler
import com.moira.dundunbackend.global.auth.JwtProvider
import com.moira.dundunbackend.global.auth.TokenType
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_REFRESH_TOKEN
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_RTK_EXPIRE_SEC
import com.moira.dundunbackend.global.common.TokenHasher
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    // repository
    private val userRepository: UserRepository,

    // service
    private val loginTxService: LoginTxService,
    private val userService: UserService,

    // utility
    private val cookieHandler: CookieHandler,
    private val jwtProvider: JwtProvider
) {
    /**
     * 로그아웃
     */
    @Transactional(propagation = Propagation.REQUIRED)
    fun logout(httpReq: HttpServletRequest, httpRes: HttpServletResponse) {
        // 로그아웃은 완벽한 정합성을 요구하는 기능이 아니므로 refreshToken 혹은 userId가 null인 경우 예외 처리하지 않음.
        // 1. 쿠키에서 refreshToken 추출
        val refreshToken = cookieHandler.get(httpReq, COOKIE_REFRESH_TOKEN)

        refreshToken
            // 2. userId, role 추출
            ?.let { jwtProvider.parseUserInfo(it, TokenType.REFRESH_TOKEN).getOrNull() }
            // 3. User 조회
            ?.let { (userId, roleStr) ->
                val role = UserRole.valueOf(roleStr)
                userRepository.findByUserIdAndRoleAndStatus(userId, role, UserStatus.ACTIVE)
            }
            // 4. refreshToken null 처리
            ?.let { it.refreshToken = null }

        // 5. 쿠키 무효화
        cookieHandler.expire(httpRes, COOKIE_REFRESH_TOKEN)
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    fun reissueToken(httpReq: HttpServletRequest, httpRes: HttpServletResponse): LoginResponse {
        // 1. 쿠키에서 refreshToken 추출
        val refreshToken = cookieHandler.get(httpReq, COOKIE_REFRESH_TOKEN)
            ?: throw CommonException(ErrorCode.INVALID_REFRESH_TOKEN)

        // 2. 토큰에서 userId와 role 추출
        val (userId, roleStr) = jwtProvider.parseUserInfo(refreshToken, TokenType.REFRESH_TOKEN).getOrElse {
            when (it) {
                is ExpiredJwtException -> throw CommonException(ErrorCode.EXPIRED_USER_INFO)
                else -> throw CommonException(ErrorCode.INVALID_REFRESH_TOKEN)
            }
        }
        val role = UserRole.valueOf(roleStr)

        // 3. User 조회
        val user = userRepository.findByUserIdAndRoleAndStatus(userId, role, UserStatus.ACTIVE)
            ?: throw CommonException(ErrorCode.INVALID_REFRESH_TOKEN)

        // 4. 유효성 검사 -> 토큰이 일치하지 않다면 기존 토큰 무효화
        if (!TokenHasher.matches(refreshToken, user.refreshToken)) {
            loginTxService.nullRefreshToken(userId)
            cookieHandler.expire(httpRes, COOKIE_REFRESH_TOKEN) // 쿠키 만료 처리

            throw CommonException(ErrorCode.INVALID_REFRESH_TOKEN)
        }

        // 5. 토큰 재발급 후 교체
        val (newAccessToken, newRefreshToken) = jwtProvider.createTokens(user)
        user.refreshToken = TokenHasher.sha256(newRefreshToken)

        // 6. refreshToken을 쿠키에 저장
        cookieHandler.add(
            httpRes = httpRes,
            cookieName = COOKIE_REFRESH_TOKEN,
            value = newRefreshToken,
            expireSec = COOKIE_RTK_EXPIRE_SEC
        )

        // 7. 사용자의 프로필 사진 조회
        var imageUrl: String? = null
        user.fileId?.let { imageUrl = userService.getUserProfileImageUrl(user.userId, user.fileId!!) }

        // 8. LoginResponse 리턴
        return LoginResponse(
            token = jwtProvider.getTokenResponse(newAccessToken),
            user = user.toSimpleResponse(imageUrl)
        )
    }
}