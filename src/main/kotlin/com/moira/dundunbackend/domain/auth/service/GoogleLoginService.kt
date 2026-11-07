package com.moira.dundunbackend.domain.auth.service

import com.moira.dundunbackend.domain.auth.component.GoogleLoginApiClient
import com.moira.dundunbackend.domain.auth.dto.request.GoogleLoginRequest
import com.moira.dundunbackend.domain.auth.dto.response.GoogleLoginUrlResponse
import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.entity.UserStatus
import com.moira.dundunbackend.domain.user.repository.UserRepository
import com.moira.dundunbackend.global.auth.CookieHandler
import com.moira.dundunbackend.global.auth.JwtProvider
import com.moira.dundunbackend.global.common.CommonVariables.BEARER
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_GOOGLE_STATE
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_REFRESH_TOKEN
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_RTK_EXPIRE_SEC
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_STATE_EXPIRE_SEC
import com.moira.dundunbackend.global.common.CommonVariables.GOOGLE_AUTH_URL
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.util.*

@Service
class GoogleLoginService(
    // repository
    private val userRepository: UserRepository,

    // service
    private val loginTxSerice: LoginTxService,

    // utility
    private val cookieHandler: CookieHandler,
    private val jwtProvider: JwtProvider,
    private val googleLoginApiClient: GoogleLoginApiClient,

    // variables
    @Value($$"${google.client-id}")
    private val clientId: String,

    @Value($$"${google.client-secret}")
    private val clientSecret: String,

    @Value($$"${google.redirect-uri}")
    private val redirectUri: String
) {
    private val log = KotlinLogging.logger {}

    /**
     * 구글 로그인 URL 전송 (to 프론트엔드)
     */
    fun getLoginUrl(httpRes: HttpServletResponse): GoogleLoginUrlResponse {
        // 1. state 생성
        val state = UUID.randomUUID().toString().replace("-", "")

        // 2. 쿠키에 state 저장
        cookieHandler.add(httpRes, COOKIE_GOOGLE_STATE, state, COOKIE_STATE_EXPIRE_SEC)

        // 3. 구글 로그인 URL 생성
        val googleAuthUrl = GOOGLE_AUTH_URL +
                "?client_id=$clientId" +
                "&redirect_uri=${URLEncoder.encode(redirectUri, "UTF-8")}" +
                "&response_type=code" +
                "&scope=${URLEncoder.encode("openid profile email", "UTF-8")}" +
                "&state=$state" +
                "&prompt=select_account"

        return GoogleLoginUrlResponse(googleAuthUrl, state)
    }

    /**
     * 구글 로그인
     */
    fun googleLogin(
        request: GoogleLoginRequest,
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): LoginResponse {
        // 1. 쿠키에 저장된 state 검증 -> 만료 처리
        val stateFromCookie = cookieHandler.get(httpReq, COOKIE_GOOGLE_STATE)

        if (stateFromCookie == null || stateFromCookie != request.state) {
            throw CommonException(ErrorCode.NO_STATE)
        } else {
            cookieHandler.expire(httpRes, COOKIE_GOOGLE_STATE) // 만료 처리
        }

        // 2. 인가 코드를 사용해 구글 토큰 발급
        val tokenResponse = runCatching {
            googleLoginApiClient.getAccessToken(
                grantType = "authorization_code",
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = redirectUri,
                code = request.code
            )
        }.getOrElse {
            log.error(it) { "[GoogleLoginService] 구글 로그인 토큰 발급 과정에서 오류 발생!" }
            throw CommonException(ErrorCode.GOOGLE_LOGIN_GET_TOKEN_FAILED)
        }

        // 3. 사용자 정보 조회
        val userResponse = runCatching {
            googleLoginApiClient.getUserInfo(authorization = "$BEARER ${tokenResponse.accessToken}")
        }.getOrElse {
            log.error(it) { "[GoogleLoginService] 구글 로그인 사용자 정보 조회 과정에서 오류 발생!" }
            throw CommonException(ErrorCode.GOOGLE_LOGIN_GET_USER_INFO_FAILED)
        }

        val oauthId = userResponse.sub
        val isValidGoogleEmail = userResponse.emailVerified ?: false
        val emailFromGoogle = if (isValidGoogleEmail) userResponse.email else null
        val nicknameFromGoogle = userResponse.name

        // 4. User 조회 -> [1] 신규 유저면 회원가입 처리 [2] 기존 유저면 회원탈퇴 여부 및 이메일 변경 여부 확인
        var user = userRepository.findByOauthIdAndOauthProvider(oauthId, UserOauthProvider.GOOGLE)
        val isNewUser = user == null

        if (isNewUser) {
            user = loginTxSerice.signup(
                oauthId = oauthId,
                email = emailFromGoogle,
                nickname = nicknameFromGoogle,
                oauthProvider = UserOauthProvider.GOOGLE
            )
        } else {
            if (user.status == UserStatus.DELETED) {
                throw CommonException(ErrorCode.DELETED_USER_CANNOT_LOGIN)
            }

            if (!emailFromGoogle.isNullOrBlank() && isValidGoogleEmail && user.email != emailFromGoogle) {
                loginTxSerice.updateEmail(user.userId, emailFromGoogle)
            }
        }

        // 5. JWT 토큰 발급 후 리턴
        val (accessToken, refreshToken) = jwtProvider.createTokens(user)

        // 6. User의 refreshToken 저장
        loginTxSerice.updateRefreshToken(user.userId, refreshToken)

        // 7. refreshToken을 쿠키에 저장
        cookieHandler.add(
            httpRes = httpRes,
            cookieName = COOKIE_REFRESH_TOKEN,
            value = refreshToken,
            expireSec = COOKIE_RTK_EXPIRE_SEC
        )

        // 8. LoginResponse 리턴
        return LoginResponse(
            token = jwtProvider.getTokenResponse(accessToken),
            user = user.toSimpleResponse()
        )
    }
}