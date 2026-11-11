package com.moira.dundunbackend.global.s3

import com.moira.dundunbackend.domain.image.entity.ImageFile
import com.moira.dundunbackend.domain.image.entity.ImageFileIdentifier
import com.moira.dundunbackend.global.common.CommonVariables.S3_PRESIGNED_URL_EXPIRES_MIN
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class AwsS3Handler(
    // utility
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,

    // variables
    @Value($$"${cloud.aws.s3.bucketName}")
    private val bucketName: String
) {
    private val log = KotlinLogging.logger {}

    /**
     * Presigend Url 발급
     */
    fun presignedUrl(image: ImageFile): String? {
        return runCatching {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(image.s3Key)
                .contentType(image.contentType)
                .contentLength(image.size)
                .build()

            val presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(S3_PRESIGNED_URL_EXPIRES_MIN)) // 10분
                .putObjectRequest(putObjectRequest)
                .build()

            val presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString()

            log.info {
                """
                
                [AwsS3Handler] PresignedUrl 발급 성공!
                - fileId: ${image.fileId}
                - fileSeqNo: ${image.fileSeqNo}
                - s3Key: ${image.s3Key}
                """.trimIndent()
            }

            presignedUrl
        }.getOrElse {
            log.error(it) {
                """
                
                [AwsS3Handler] PresignedUrl 발급 실패!
                - fileId: ${image.fileId}
                - fileSeqNo: ${image.fileSeqNo}
                - s3Key: ${image.s3Key}
                """.trimIndent()
            }

            // 예외 처리하지 않고 null 리턴
            null
        }
    }

    /**
     * 조회용 Presigned Url 발급
     */
    fun presignedGetUrl(s3Key: String): String? {
        return runCatching {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build()

            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(S3_PRESIGNED_URL_EXPIRES_MIN)) // 10분
                .getObjectRequest(getObjectRequest)
                .build()

            s3Presigner.presignGetObject(presignRequest).url().toString()
        }.getOrElse {
            log.error(it) {
                """
                
                [AwsS3Handler] 조회용 PresignedUrl 발급 실패!
                - s3Key: $s3Key
                """.trimIndent()
            }

            // 예외 처리하지 않고 null 리턴
            null
        }
    }

    /**
     * AWS S3 Key값 생성
     */
    fun getS3Key(identifier: ImageFileIdentifier, fileId: String, fileSeqNo: Int, extension: String) =
        "dundun/${identifier.name}/${fileId}/${fileSeqNo}.${extension}"

    /**
     * S3 버킷에 해당 객체가 존재하는지 여부
     */
    fun hasObject(s3Key: String): Boolean {
        return try {
            s3Client.headObject(
                HeadObjectRequest.builder().bucket(bucketName).key(s3Key).build()
            )
            true
        } catch (_: NoSuchKeyException) {
            false
        } catch (e: Exception) {
            log.error(e) {
                """
                
                [AwsS3Handler] 객체 조회 실패!
                - s3Key: $s3Key
                """.trimIndent()
            }
            false
        }
    }

    /**
     * S3 버킷에서 객체 삭제
     */
    fun deleteObject(s3Key: String): Boolean {
        val deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build()

        return runCatching {
            s3Client.deleteObject(deleteRequest)

            log.info {
                """
                
                [AwsS3Handler] 파일 삭제 성공!
                - s3Key: $s3Key
                """.trimIndent()
            }

            true
        }.getOrElse {
            log.error(it) {
                """
                
                [AwsS3Handler] 파일 삭제 실패!
                - s3Key: $s3Key
                """.trimIndent()
            }

            // 예외 처리하지 않고 false 리턴
            false
        }
    }
}