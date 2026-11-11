package com.moira.dundunbackend.domain.image.entity

import java.io.Serializable

data class ImageFileId(
    val fileId: String = "",
    val fileSeqNo: Int = 0
) : Serializable