package org.example.expert.domain.user.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserChangePasswordRequest(
    @field:NotBlank(message = "기존 비밀번호는 필수입니다.")
    val oldPassword: String = "",

    @field:NotBlank(message = "새 비밀번호는 필수입니다.")
    @field:Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[A-Z]).*$",
        message = "새 비밀번호는 숫자와 대문자를 포함해야 합니다."
    )
    val newPassword: String = ""
)