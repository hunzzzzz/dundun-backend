package com.moira.dundunbackend.domain.user.service

import com.moira.dundunbackend.domain.group.repository.UserGroupMemberRepository
import com.moira.dundunbackend.domain.group.service.UserGroupService
import com.moira.dundunbackend.domain.image.entity.ImageFileIdentifier
import com.moira.dundunbackend.domain.image.repository.ImageFileRepository
import com.moira.dundunbackend.domain.user.dto.request.UserImageUpdateRequest
import com.moira.dundunbackend.domain.user.dto.request.UserNicknameUpdateRequest
import com.moira.dundunbackend.domain.user.dto.response.MyPageResponse
import com.moira.dundunbackend.domain.user.entity.User
import com.moira.dundunbackend.domain.user.entity.UserStatus
import com.moira.dundunbackend.domain.user.repository.UserRepository
import com.moira.dundunbackend.global.auth.CookieHandler
import com.moira.dundunbackend.global.common.CommonVariables.COOKIE_REFRESH_TOKEN
import com.moira.dundunbackend.global.exception.CommonException
import com.moira.dundunbackend.global.exception.ErrorCode
import com.moira.dundunbackend.global.s3.AwsS3Handler
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    // repository
    private val imageFileRepository: ImageFileRepository,
    private val userGroupMemberRepository: UserGroupMemberRepository,
    private val userRepository: UserRepository,
    // service
    private val userGroupService: UserGroupService,
    // utility
    private val awsS3Handler: AwsS3Handler,
    private val cookieHandler: CookieHandler
) {
    /**
     * (공통) 사용자 프로필 사진 (presigned) URL 조회
     */
    fun getUserProfileImageUrl(userId: String, fileId: String): String? {
        return imageFileRepository.findByFileIdAndFileSeqNoAndUserIdAndIdentifier(
            fileId = fileId,
            fileSeqNo = 1,
            userId = userId,
            identifier = ImageFileIdentifier.USER_IMAGE
        )?.let { awsS3Handler.presignedGetUrl(it.s3Key) }
    }

    /**
     * 마이페이지 조회
     */
    @Transactional(readOnly = true)
    fun mypage(userId: String): MyPageResponse {
        // 1. MyPageResponse 조회
        val response = userRepository.selectMyPageResponse(userId) ?: throw CommonException(ErrorCode.USER_NOT_FOUND)

        // 2. ImageFile 조회
        if (response.fileId != null) {
            response.imageUrl = this.getUserProfileImageUrl(response.userId, response.fileId)
        }

        // 3. 리턴
        return response
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    fun updateNickname(userId: String, request: UserNicknameUpdateRequest) {
        // 1. 유효성 검사
        if (userRepository.existsByNickname(request.newNickname)) {
            throw CommonException(ErrorCode.USING_NICKNAME)
        }

        // 2. User 조회
        val user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
            ?: throw CommonException(ErrorCode.USER_NOT_FOUND)

        // 3. nickname 변경
        user.nickname = request.newNickname
    }

    /**
     * 프로필 사진 변경
     */
    @Transactional
    fun updateImage(userId: String, request: UserImageUpdateRequest) {
        // 1. ImageFile 조회
        val imageFile = imageFileRepository.findByFileIdAndFileSeqNoAndUserIdAndIdentifier(
            fileId = request.fileId,
            fileSeqNo = 1,
            userId = userId,
            identifier = ImageFileIdentifier.USER_IMAGE
        ) ?: throw CommonException(ErrorCode.IMAGE_FILE_NOT_FOUND)

        // 2. 유효성 검사 (S3 버킷에 객체 존재 여부 조회)
        if (!awsS3Handler.hasObject(imageFile.s3Key)) {
            throw CommonException(ErrorCode.S3_CHECK_OBJECT_FAILED)
        }

        // 3. User 조회
        val user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
            ?: throw CommonException(ErrorCode.USER_NOT_FOUND)

        // 4. fileId 변경
        user.fileId = request.fileId
    }

    /**
     * 회원탈퇴 (soft delete)
     */
    @Transactional
    fun delete(userId: String, httpRes: HttpServletResponse) {
        // 1. User 조회
        val user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
            ?: throw CommonException(ErrorCode.USER_NOT_FOUND)

        // 2. 사용자가 참여한 모든 그룹 ID 조회
        val groupIds = userGroupMemberRepository.selectGroupIdListByUserId(user.userId)

        // TODO: 3. 기록 정보 삭제

        // 4. 그룹 탈퇴
        groupIds.forEach { groupId -> userGroupService.leave(userId, groupId) }

        // 5. User의 status를 DELETED로 변경 + refreshToken 초기화
        user.status = UserStatus.DELETED
        user.deletedAt = LocalDateTime.now()
        user.refreshToken = null

        // 6. 로그아웃 처리
        cookieHandler.expire(httpRes, COOKIE_REFRESH_TOKEN)
    }

    /**
     * 회원탈퇴 (hard delete) (from UserDeleteScheduler)
     */
    @Transactional
    fun delete(user: User): Boolean {
        // 1. 해당 사용자가 등록한 모든 ImageFile 조회
        val imageFiles = imageFileRepository.findAllByUserId(user.userId)

        // 2. S3에서 모든 이미지 객체 삭제
        // 객체 삭제 시 에러가 발생하면 (= deleteObject의 리턴값이 false) ImageFile들을 삭제하지 않고 다음 스케쥴러로 위임)
        val results = imageFiles.map { awsS3Handler.deleteObject(it.s3Key) }
        if (!results.all { it }) return false

        // 3. DB 상에서 ImageFile 삭제
        imageFileRepository.deleteAll(imageFiles)

        // TODO: 4. 기록 정보 삭제

        // 5. User 삭제
        userRepository.delete(user)

        return true
    }
}