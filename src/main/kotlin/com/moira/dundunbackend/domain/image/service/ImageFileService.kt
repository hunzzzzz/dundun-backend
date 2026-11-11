package com.moira.dundunbackend.domain.image.service

import com.moira.dundunbackend.domain.image.dto.request.PresignedUrlRequest
import com.moira.dundunbackend.domain.image.dto.response.PresignedUrlItemResponse
import com.moira.dundunbackend.domain.image.dto.response.PresignedUrlResponse
import com.moira.dundunbackend.domain.image.entity.ImageFile
import com.moira.dundunbackend.domain.image.repository.ImageFileRepository
import com.moira.dundunbackend.global.common.CommonUtils
import com.moira.dundunbackend.global.common.CommonVariables.IMAGE_MAX_SIZE_BYTES
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import com.moira.dundunbackend.global.s3.AwsS3Handler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ImageFileService(
    // repository
    private val imageFileRepository: ImageFileRepository,
    // utility
    private val awsS3Handler: AwsS3Handler
) {
    private val allowedContentTypes = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp",
        "image/heic" to "heic"
    )

    /**
     * AWS S3 Presigned Url 발급
     */
    @Transactional
    fun presignedUrl(userId: String, request: PresignedUrlRequest): PresignedUrlResponse {
        // 1. 유효성 검사
        if (request.items.size !in 1..request.identifier.maxFileCnt) { // 파일 개수
            throw CommonException(ErrorCode.INVALID_FILE_COUNT)
        }
        if (request.items.map { it.isFirstImage }.count { it } != 1) { // isFirstImage인 이미지 파일은 1개만 있어야 한다.
            throw CommonException(ErrorCode.INVALID_PARAMETER)
        }
        if (request.items.any { it.size !in 1..IMAGE_MAX_SIZE_BYTES }) { // 단일 파일이 2MB를 초과할 수 없다.
            throw CommonException(ErrorCode.FILE_SIZE_EXCEEDED)
        }

        // 2. 파라미터 세팅
        val identifier = request.identifier
        val images = mutableListOf<ImageFile>()
        val fileId = CommonUtils.createFileId()
        val items = mutableListOf<PresignedUrlItemResponse>()

        request.items.forEachIndexed { index, itemRequest ->
            val fileSeqNo = index + 1
            val extension = this.allowedContentTypes[itemRequest.contentType]
                ?: throw CommonException(ErrorCode.NOT_ALLOWED_EXTENSIONS)
            val originalFileName = if (itemRequest.originalFileName.length > 255) {
                "image${fileSeqNo}.${extension}"
            } else itemRequest.originalFileName

            val s3Key = awsS3Handler.getS3Key(
                identifier = identifier,
                fileId = fileId,
                fileSeqNo = fileSeqNo,
                extension = extension
            )
            val contentType = itemRequest.contentType

            // 3. ImageFile 객체 생성
            val image = ImageFile(
                fileId = fileId,
                fileSeqNo = fileSeqNo,
                userId = userId,
                originalFileName = originalFileName,
                s3Key = s3Key,
                size = itemRequest.size,
                contentType = contentType,
                identifier = identifier,
                isFirstImage = itemRequest.isFirstImage
            )

            // 4. Presigned Url 발급
            val presignedUrl = awsS3Handler.presignedUrl(image)
                ?: throw CommonException(ErrorCode.S3_PRESIGNED_URL_CREATE_FAILED)

            // 5. PresignedUrlItemResponse 객체 생성
            val presignedUrlItemResponse = PresignedUrlItemResponse(
                fileSeqNo = fileSeqNo,
                presignedUrl = presignedUrl,
                isFirstImage = image.isFirstImage,
            )

            // 6. 각 리스트(files, items)에 add
            images.add(image)
            items.add(presignedUrlItemResponse)
        }

        // 7. ImageFile 목록 저장
        imageFileRepository.saveAll(images)

        // 8. PresignedUrlResponse 객체 생성 후 리턴
        return PresignedUrlResponse(
            fileId = fileId,
            items = items
        )
    }

    /**
     * 파일 삭제 (from ImageFileDeleteScheduler)
     */
    @Transactional
    fun delete(imageFile: ImageFile): Boolean {
        // 1. S3 객체 삭제
        if (!awsS3Handler.deleteObject(imageFile.s3Key)) return false

        // 2. ImageFile 삭제
        imageFileRepository.delete(imageFile)

        return true
    }
}