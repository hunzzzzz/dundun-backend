package com.moira.dundunbackend.domain.auth.service

import com.moira.dundunbackend.domain.auth.component.KakaoLoginApiClient
import com.moira.dundunbackend.domain.auth.dto.request.KakaoLoginRequest
import com.moira.dundunbackend.domain.auth.dto.response.KakaoLoginUrlResponse
import com.moira.dundunbackend.domain.auth.dto.response.LoginResponse
import com.moira.dundunbackend.domain.image.repository.ImageFileRepository
import com.moira.dundunbackend.domain.user.entity.UserOauthProvider
import com.moira.dundunbackend.domain.user.entity.UserStatus
import com.moira.dundunbackend.domain.user.repository.UserRepository
import com.moira.dundunbackend.domain.user.service.UserService
import com.moira.dundunbackend.global.auth.CookieHandler
import com.moira.dundunbackend.global.auth.JwtProvider
import com.moira.dundunbackend.global.common.CommonVariables.BEARER
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_KAKAO_STATE
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_REFRESH_TOKEN
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_RTK_EXPIRE_SEC
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_STATE_EXPIRE_SEC
import com.moira.dundunbackend.global.common.CommonVariables.KAKAO_AUTH_URL
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
class KakaoLoginService(
    // repository
    private val imageFileRepository: ImageFileRepository,
    private val userRepository: UserRepository,

    // service
    private val loginTxSerice: LoginTxService,
    private val userService: UserService,

    // utility
    private val cookieHandler: CookieHandler,
    private val jwtProvider: JwtProvider,
    private val kakaoLoginApiClient: KakaoLoginApiClient,

    // variables
    @Value($$"${kakao.client-id}")
    private val clientId: String,

    @Value($$"${kakao.client-secret}")
    private val clientSecret: String,

    @Value($$"${kakao.redirect-uri}")
    private val redirectUri: String
) {
    private val log = KotlinLogging.logger {}

    /**
     * 카카오 로그인 URL 전송 (to 프론트엔드)
     */
    fun getLoginUrl(httpRes: HttpServletResponse): KakaoLoginUrlResponse {
        // 1. state 생성
        val state = UUID.randomUUID().toString().replace("-", "")

        // 2. 쿠키에 state 저장
        cookieHandler.add(httpRes, COOKIE_KAKAO_STATE, state, COOKIE_STATE_EXPIRE_SEC)

        // 3. 카카오 로그인 URL 생성
        val kakaoAuthUrl = KAKAO_AUTH_URL +
                "?client_id=$clientId" +
                "&redirect_uri=${URLEncoder.encode(redirectUri, "UTF-8")}" +
                "&response_type=code" +
                "&state=$state"

        return KakaoLoginUrlResponse(kakaoAuthUrl, state)
    }

    /**
     * 카카오 로그인
     */
    fun kakaoLogin(
        request: KakaoLoginRequest,
        httpReq: HttpServletRequest,
        httpRes: HttpServletResponse
    ): LoginResponse {
        // 1. 쿠키에 저장된 state 검증 -> 만료 처리
        val stateFromCookie = cookieHandler.get(httpReq, COOKIE_KAKAO_STATE)

        if (stateFromCookie == null || stateFromCookie != request.state) {
            throw CommonException(ErrorCode.NO_STATE)
        } else {
            cookieHandler.expire(httpRes, COOKIE_KAKAO_STATE) // 만료 처리
        }

        // 2. 인가 코드를 사용해 카카오 토큰 발급
        val tokenResponse = runCatching {
            kakaoLoginApiClient.getAccessToken(
                grantType = "authorization_code",
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = redirectUri,
                code = request.code
            )
        }.getOrElse {
            log.error(it) { "[KakaoLoginService] 카카오 로그인 토큰 발급 과정에서 오류 발생!" }
            throw CommonException(ErrorCode.KAKAO_LOGIN_GET_TOKEN_FAILED)
        }

        // 3. 사용자 정보 조회
        val userResponse = runCatching {
            kakaoLoginApiClient.getUserInfo(authorization = "$BEARER ${tokenResponse.accessToken}")
        }.getOrElse {
            log.error(it) { "[KakaoLoginService] 카카오 로그인 사용자 정보 조회 과정에서 오류 발생!" }
            throw CommonException(ErrorCode.KAKAO_LOGIN_GET_USER_INFO_FAILED)
        }

        val oauthId = userResponse.id.toString()
        val emailFromKakao = userResponse.account.email
        val nicknameFromKakao = userResponse.account.profile.nickname

        // 4. User 조회 -> [1] 신규 유저면 회원가입 처리 [2] 기존 유저면 회원탈퇴 여부 및 이메일 변경 여부 확인
        var user = userRepository.findByOauthIdAndOauthProvider(oauthId, UserOauthProvider.KAKAO)
        val isNewUser = user == null

        if (isNewUser) {
            user = loginTxSerice.signup(
                oauthId = oauthId,
                email = emailFromKakao,
                nickname = nicknameFromKakao,
                oauthProvider = UserOauthProvider.KAKAO
            )
        } else {
            if (user.status == UserStatus.DELETED) {
                throw CommonException(ErrorCode.DELETED_USER_CANNOT_LOGIN)
            }

            if (!emailFromKakao.isNullOrBlank() && user.email != emailFromKakao) {
                loginTxSerice.updateEmail(user.userId, emailFromKakao)
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

        // 8. 사용자의 프로필 사진 조회
        var imageUrl: String? = null
        user.fileId?.let { imageUrl = userService.getUserProfileImageUrl(user.userId, user.fileId!!) }

        // 9. LoginResponse 리턴
        return LoginResponse(
            token = jwtProvider.getTokenResponse(accessToken),
            user = user.toSimpleResponse(imageUrl)
        )
    }
}