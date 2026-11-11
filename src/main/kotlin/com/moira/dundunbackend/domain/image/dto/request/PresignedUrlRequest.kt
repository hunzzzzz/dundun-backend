package com.moira.dundunbackend.domain.image.dto.request

import com.moira.dundunbackend.domain.image.entity.ImageFileIdentifier

data class PresignedUrlRequest(
    val identifier: ImageFileIdentifier,
    val items: List<PresignedUrlItemRequest>
)
