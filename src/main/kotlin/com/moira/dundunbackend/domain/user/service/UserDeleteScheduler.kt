package com.moira.dundunbackend.domain.user.service

import com.moira.dundunbackend.domain.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserDeleteScheduler(
    // repository
    private val userRepository: UserRepository,
    // service
    private val userService: UserService
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    fun delete() {
        // 1. 회원탈퇴일로부터 60일이 지난 User 목록 조회
        val users = userRepository.selectAllForDelete()
        var successCnt = 0
        var failedCnt = 0

        log.info {
            """
            
            [UserDeleteScheduler] 스케줄러 작동 시작.
            - 삭제 대상: ${users.size}건
            """.trimIndent()
        }

        users.forEach { user ->
            runCatching { userService.delete(user) }
                .onSuccess { if (it) successCnt++ else failedCnt++ }
                .onFailure { // 다음 사용자 삭제에 영향을 주지 않도록 Exception throw하지 않음
                    failedCnt++

                    log.error(it) {
                        """
                        
                        [UserDeleteScheduler] 사용자 삭제 실패!
                        - userId: ${user.userId}
                        """.trimIndent()
                    }
                }
        }

        log.info {
            """
            
            [UserDeleteScheduler] 스케줄러 종료.
            - 삭제 대상: ${users.size}건
            - 삭제 성공: ${successCnt}건
            - 삭제 실패: ${failedCnt}건
            """.trimIndent()
        }
    }
}