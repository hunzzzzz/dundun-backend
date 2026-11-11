package com.moira.dundunbackend.domain.image.entity

import com.moira.dundunbackend.domain.base.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate

@Entity
@IdClass(ImageFileId::class)
@DynamicUpdate
@Table(name = "image_file", schema = "dundun")
class ImageFile(
    @Id
    @Column(name = "file_id", nullable = false)
    var fileId: String,

    @Id
    @Column(name = "file_seq_no", nullable = false)
    var fileSeqNo: Int,

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "original_file_name", nullable = false)
    var originalFileName: String,

    @Column(name = "s3_key", nullable = false, unique = true)
    var s3Key: String,

    @Column(name = "size", nullable = false)
    var size: Long,

    @Column(name = "content_type", nullable = false)
    var contentType: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier", nullable = false)
    var identifier: ImageFileIdentifier,

    @Column(name = "is_first_image", nullable = false)
    var isFirstImage: Boolean
) : BaseEntity()