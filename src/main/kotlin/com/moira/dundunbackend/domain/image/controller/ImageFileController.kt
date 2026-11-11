package com.moira.dundunbackend.domain.image.controller

import com.moira.dundunbackend.domain.image.dto.request.PresignedUrlRequest
import com.moira.dundunbackend.domain.image.dto.response.PresignedUrlResponse
import com.moira.dundunbackend.domain.image.service.ImageFileService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ImageFileController(
    private val imageFileService: ImageFileService
) {
    /**
     * AWS S3 Presigned Url 발급
     */
    @PostMapping("/api/files/presigned-url")
    fun presignedUrl(
        @AuthenticationPrincipal userId: String,
        @RequestBody request: PresignedUrlRequest
    ): ResponseEntity<PresignedUrlResponse> {
        val response = imageFileService.presignedUrl(userId, request)

        return ResponseEntity.ok().body(response)
    }
}