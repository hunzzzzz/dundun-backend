package com.moira.dundunbackend.global.common

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*

object TokenHasher {
    /**
     * SHA256 암호화
     */
    fun sha256(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(digest) // 64자 hex
    }

    /**
     * 일치 여부 검증
     */
    fun matches(rawToken: String, storedHash: String?): Boolean {
        if (storedHash == null) return false
        return MessageDigest.isEqual(
            sha256(rawToken).toByteArray(StandardCharsets.UTF_8),
            storedHash.toByteArray(StandardCharsets.UTF_8)
        )
    }
}