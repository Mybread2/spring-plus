package org.example.expert.domain.user.dto.request

import jakarta.validation.constraints.NotBlank

data class UserRoleChangeRequest(
    @field:NotBlank(message = "역할은 필수입니다.")
    val role: String = ""
)