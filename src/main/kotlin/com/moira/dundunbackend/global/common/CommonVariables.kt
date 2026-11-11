package com.moira.dundunbackend.global.common

object CommonVariables {
    const val LOCAL = "local"
    const val BEARER = "Bearer"
    const val EXPIRATION_TIME_ATK = 1000 * 60 * 15L // 15분
    const val EXPIRATION_TIME_RTK = 1000 * 60 * 60 * 24 * 2L // 2일

    const val COOKIE_KAKAO_STATE = "kakao_state"
    const val COOKIE_GOOGLE_STATE = "google_state"
    const val COOKIE_REFRESH_TOKEN = "refresh_token"
    const val COOKIE_STATE_EXPIRE_SEC = 5 * 60 // 5분
    const val COOKIE_RTK_EXPIRE_SEC = EXPIRATION_TIME_RTK.toInt() / 1000 // 2일

    const val KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize"
    const val KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
    const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
    const val KAKAO_PLACE_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/keyword"

    const val GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth"
    const val GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
    const val GOOGLE_USER_INFO_URL = "https://openidconnect.googleapis.com/v1/userinfo"

    const val GROUP_JOIN_MAX_COUNT = 5
    const val GROUP_MEMBERS_MAX_COUNT = 10

    const val S3_PRESIGNED_URL_EXPIRES_MIN = 10L
    const val IMAGE_MAX_SIZE_BYTES = 1000 * 1000 * 2L // 2MB
}