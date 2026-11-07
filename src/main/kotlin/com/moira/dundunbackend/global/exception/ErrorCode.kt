package com.moira.dundunbackend.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    val message: String
) {
    // 공통
    INTERNAL_SERVER_ERROR(
        code = "COMM01",
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        message = "알 수 없는 오류가 발생했습니다. 해당 오류가 반복되는 경우 고객센터로 문의해주세요."
    ),
    FORBIDDEN(
        code = "COMM02",
        httpStatus = HttpStatus.FORBIDDEN,
        message = "비정상적인 접근입니다."
    ),
    INVALID_ENUM(
        code = "COMM03",
        httpStatus = HttpStatus.FORBIDDEN,
        message = "잘못된 enum 값입니다."
    ),
    INVALID_REQUEST_BODY(
        code = "COMM04",
        httpStatus = HttpStatus.BAD_REQUEST,
        message = "잘못된 요청입니다."
    ),
    INVALID_PARAMETER(
        code = "COMM05",
        httpStatus = HttpStatus.BAD_REQUEST,
        message = "유효하지 않은 파라미터입니다."
    ),

    EXPIRED_ACCESS_TOKEN(
        code = "AUTH01",
        httpStatus = HttpStatus.UNAUTHORIZED,
        message = "액세스 토큰이 만료되었습니다. 토큰을 재발급해주세요."
    ),
    INVALID_ACCESS_TOKEN(
        code = "AUTH02",
        httpStatus = HttpStatus.UNAUTHORIZED,
        message = "올바르지 않은 토큰입니다. 다시 로그인해 주세요."
    ),
    INVALID_AUTHORIZATION_HEADER(
        code = "AUTH03",
        httpStatus = HttpStatus.BAD_REQUEST,
        message = "Authorization 헤더에 담긴 토큰 정보가 유효하지 않습니다."
    ),
}