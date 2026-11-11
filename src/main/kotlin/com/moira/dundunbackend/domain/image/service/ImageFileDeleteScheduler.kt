package com.moira.dundunbackend.domain.image.service

import com.moira.dundunbackend.domain.image.repository.ImageFileRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ImageFileDeleteScheduler(
    // repository
    private val imageFileRepository: ImageFileRepository,
    // service
    private val imageFileService: ImageFileService
) {
    private val log = KotlinLogging.logger {}

    /**
     * 실제 파일 삭제
     */
    @Scheduled(cron = "0 0/30 * * * *", zone = "Asia/Seoul") // 30분 간격
    fun delete() {
        // 1. 삭제 대상인 ImageFile 목록을 조회 (업로드한지 한 시간이 지났으며 User/Record 어디에서도 사용되지 않는 이미지)
        val images = imageFileRepository.selectAllForDelete()
        var successCnt = 0
        var failedCnt = 0

        log.info {
            """
            
            [ImageFileDeleteScheduler] 스케줄러 작동 시작.
            - 삭제 대상: ${images.size}건
            """.trimIndent()
        }

        // 2. 각 파일 별로 트랜잭션을 분리하여 삭제 (S3 삭제 + DB 삭제)
        images.forEach { image ->
            runCatching { imageFileService.delete(image) }
                .onSuccess { if (it) successCnt++ else failedCnt++ }
                .onFailure { // 다음 파일 삭제에 영향을 주지 않도록 Exception throw하지 않음
                    failedCnt++

                    log.error(it) {
                        """
                        
                        [ImageFileDeleteScheduler] 파일 삭제 실패!
                        - fileId: ${image.fileId}
                        - fileSeqNo: ${image.fileSeqNo}
                        - s3Key: ${image.s3Key}
                        """.trimIndent()
                    }
                }
        }

        log.info {
            """
            
            [ImageFileDeleteScheduler] 스케줄러 종료.
            - 삭제 대상: ${images.size}건
            - 삭제 성공: ${successCnt}건
            - 삭제 실패: ${failedCnt}건
            """.trimIndent()
        }
    }
}