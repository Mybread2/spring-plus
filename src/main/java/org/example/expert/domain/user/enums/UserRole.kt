package org.example.expert.domain.user.enums

import org.example.expert.domain.common.exception.InvalidRequestException

enum class UserRole {
    ADMIN, USER;

    companion object {
        fun of(role: String?): UserRole {
            if (role.isNullOrBlank()) {
                throw IllegalArgumentException("User role cannot be null or blank")
            }

            return enumValues<UserRole>()
                .firstOrNull { it.name.equals(role.trim(), ignoreCase = true)}
                ?: throw InvalidRequestException("유효하지 않은 UserRole: $role. 사용 가능한 값: ${enumValues<UserRole>().joinToString()}")
        }
    }
}