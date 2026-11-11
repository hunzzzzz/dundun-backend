package com.moira.dundunbackend.domain.image.dto.response

data class PresignedUrlResponse(
    val fileId: String,
    val items: List<PresignedUrlItemResponse>
)
