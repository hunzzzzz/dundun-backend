package com.moira.dundunbackend.global.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import tools.jackson.databind.exc.InvalidFormatException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger {}

    private fun getErrorResponse(errorCode: ErrorCode): ErrorResponse {
        return ErrorResponse(errCode = errorCode.code, errName = errorCode.name, errMsg = errorCode.message)
    }

    /**
     * CommonException
     */
    @ExceptionHandler(CommonException::class)
    fun handleCommonException(e: CommonException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode

        // 1. 로깅
        if (errorCode.httpStatus != HttpStatus.BAD_REQUEST) {
            log.error(e) { "[CommonException] 에러 발생 - (${errorCode.httpStatus}) [${errorCode.name}] ${e.message}" }
        }

        // 2. 에러 응답값 생성 후 리턴
        return ResponseEntity.status(errorCode.httpStatus).body(this.getErrorResponse(errorCode))
    }

    /**
     * HttpMessageNotReadableException (enum 오류 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val cause = e.cause
        var errorCode = ErrorCode.INVALID_REQUEST_BODY

        // 1. enum 값인 경우는 INVALID_ENUM 예외 처리
        if (cause is InvalidFormatException && cause.targetType.isEnum) {
            errorCode = ErrorCode.INVALID_ENUM
            val propertyName = cause.path.lastOrNull()?.propertyName
            log.error(e) {
                "[HttpMessageNotReadableException] enum 변환 실패 - fieldName: ${propertyName}, value:${cause.value}"
            }
        } else {
            log.error(e) { "[HttpMessageNotReadableException] 요청 바디 파싱 실패 - ${e.message}" }
        }

        // 2. 에러 응답값 생성 후 리턴
        return ResponseEntity.status(errorCode.httpStatus).body(this.getErrorResponse(errorCode))
    }

    /**
     * MethodArgumentNotValidException (validation 오류)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorCode = e.bindingResult.fieldErrors
            .firstNotNullOfOrNull { error ->
                ErrorCode.entries.find { it.name == error.defaultMessage }
            } ?: ErrorCode.INVALID_REQUEST_BODY

        return ResponseEntity.status(errorCode.httpStatus).body(this.getErrorResponse(errorCode))
    }

    /**
     * Exception
     */
    @ExceptionHandler(Exception::class)
    fun handleAllException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR

        // 1. 로깅
        log.error(e) { "[Exception] 시스템 서버 에러 발생 - ${e.message}" }

        // 2. 에러 응답값 생성 후 리턴
        return ResponseEntity.status(errorCode.httpStatus).body(this.getErrorResponse(errorCode))
    }
}