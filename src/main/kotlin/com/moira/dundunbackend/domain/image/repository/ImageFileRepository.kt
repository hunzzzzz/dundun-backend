package com.moira.dundunbackend.domain.image.repository

import com.moira.dundunbackend.domain.image.entity.ImageFile
import com.moira.dundunbackend.domain.image.entity.ImageFileId
import com.moira.dundunbackend.domain.image.entity.ImageFileIdentifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ImageFileRepository : JpaRepository<ImageFile, ImageFileId> {
    fun findByFileIdAndFileSeqNoAndUserIdAndIdentifier(
        fileId: String,
        fileSeqNo: Int,
        userId: String,
        identifier: ImageFileIdentifier
    ): ImageFile?

    fun findAllByUserId(userId: String): List<ImageFile>

    @Query(
        """
        SELECT F
        FROM ImageFile F
        LEFT JOIN User U ON U.fileId = F.fileId
        WHERE F.createdAt <= :dayBefore1Hours
         AND (U.fileId IS NULL)
    """
    ) // TODO: 추후 수정 필요
    fun selectAllForDelete(dayBefore1Hours: LocalDateTime = LocalDateTime.now().minusHours(1)): List<ImageFile>
}