package com.moira.dundunbackend.global.security

import com.moira.dundunbackend.global.exception.ErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val errorCode = ErrorCode.FORBIDDEN

        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        response.writer.write(
            """
                {
                    "errCode": "${errorCode.code}",
                    "errName": "${errorCode.name}",
                    "errMsg": "${errorCode.message}",
                    "time": "${LocalDateTime.now()}"
                }
            """.trimIndent()
        )
    }
}