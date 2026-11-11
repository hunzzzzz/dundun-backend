package com.moira.dundunbackend.domain.image.dto.request

data class PresignedUrlItemRequest(
    val originalFileName: String,
    val size: Long,
    val contentType: String,
    val isFirstImage: Boolean,
)
