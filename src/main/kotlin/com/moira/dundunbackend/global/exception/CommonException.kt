package com.moira.dundunbackend.global.exception

class CommonException(
    val errorCode: ErrorCode
) : RuntimeException(errorCode.message)