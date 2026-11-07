package com.moira.dundunbackend.global.auth

import com.moira.dundunbackend.domain.user.entity.User
import com.moira.dundunbackend.global.common.CommonVariables.BEARER
import com.moira.dundunbackend.global.common.CommonVariables.EXPIRATION_TIME_ATK
import com.moira.dundunbackend.global.common.CommonVariables.EXPIRATION_TIME_RTK
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider {
    @Value($$"${jwt.secret}")
    lateinit var secret: String

    @Value($$"${jwt.issuer}")
    lateinit var issuer: String

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * 토큰 생성
     */
    private fun createToken(user: User, type: TokenType): String {
        // 1. 파라미터 세팅
        val now = Date()
        val claims = mapOf("type" to type.name, "role" to user.role.name)
        val expirationTime = when (type) {
            TokenType.ACCESS_TOKEN -> EXPIRATION_TIME_ATK
            TokenType.REFRESH_TOKEN -> EXPIRATION_TIME_RTK
        }

        // 2. 토큰 생성
        val token = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.userId)
            .claims(claims)
            .issuedAt(now)
            .expiration(Date(now.time + expirationTime))
            .signWith(secretKey)
            .issuer(issuer)
            .compact()

        return token
    }

    /**
     * 토큰 생성
     */
    fun createTokens(user: User): Pair<String, String> {
        // 1. accessToken, refreshToken 생성
        val accessToken = this.createToken(user, TokenType.ACCESS_TOKEN)
        val refreshToken = this.createToken(user, TokenType.REFRESH_TOKEN)

        // 2. 리턴
        return Pair(accessToken, refreshToken)
    }

    /**
     * TokenResponse 생성
     */
    fun getTokenResponse(accessToken: String): TokenResponse {
        return TokenResponse(
            grantType = BEARER,
            accessToken = accessToken,
            accessTokenExpiresTimeMillis = EXPIRATION_TIME_ATK
        )
    }

    /**
     * 토큰으로부터 userId과 role 추출
     */
    fun parseUserInfo(token: String, type: TokenType): Result<Pair<String, String>> {
        return runCatching {
            // 1. claims 추출
            val claims = Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload

            val typeStr = claims.get("type", String::class.java)
            val roleStr = claims.get("role", String::class.java)

            // 2. type, role 검증
            if (typeStr.isNullOrBlank() || roleStr.isNullOrBlank() || typeStr != type.name) {
                throw JwtException("") // 실제 예외 핸들링은 JwtAuthorizationFilter에서 진행
            }

            // 3. Pair(userId, role) 리턴
            Pair(claims.subject, roleStr)
        }
    }
}