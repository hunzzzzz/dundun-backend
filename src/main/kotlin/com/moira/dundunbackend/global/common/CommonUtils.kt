package com.moira.dundunbackend.global.common

import com.moira.dundunbackend.domain.user.entity.UserOauthProvider

object CommonUtils {
    private const val USER_IDENTIFIER = "ur"
    private const val FILE_IDENTIFIER = "fe"
    private val ALLOWED_CHARS = buildList {
        addAll(('A'..'Z').toList())
        addAll(('a'..'z').toList())
        addAll(('0'..'9').toList())
    }

    /**
     * 랜덤 ID 생성
     */
    private fun createRandomId(identifier: String): String {
        val randomPart = (1..10)
            .map { ALLOWED_CHARS.random() }
            .joinToString("")

        return "$identifier$randomPart"
    }

    fun createUserId() = this.createRandomId(USER_IDENTIFIER)
    fun createFileId() = this.createRandomId(FILE_IDENTIFIER)

    /**
     * 랜덤 닉네임 생성
     */
    fun generateRandomNickname(oauthProvider: UserOauthProvider, oauthNickname: String?) =
        "${oauthProvider.name}_${oauthNickname ?: "사용자"}_${(0..99999).random().toString().padStart(5, '0')}"

    /**
     * Enum 값 검증
     */
    inline fun <reified T : Enum<T>> isValidEnum(value: String): Boolean {
        return runCatching { enumValueOf<T>(value) }.isSuccess
    }
}