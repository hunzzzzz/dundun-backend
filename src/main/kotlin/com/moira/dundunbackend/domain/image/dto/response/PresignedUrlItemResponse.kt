package com.moira.dundunbackend.domain.image.dto.response

data class PresignedUrlItemResponse(
    val fileSeqNo: Int,
    val presignedUrl: String,
    val isFirstImage: Boolean,
)
