package org.example.expert.domain.user.entity

import org.example.expert.domain.common.exception.InvalidRequestException
import org.example.expert.global.security.PasswordEncoder

/**
 * 비밀번호 검증 전역 함수
 */
fun checkPassword(
    currentPassword: String,
    oldPassword: String,
    newPassword: String
) {
    val passwordEncoder = PasswordEncoder() // 프로젝트의 커스텀 PasswordEncoder 사용

    checkOldPasswordMatches(passwordEncoder, currentPassword, oldPassword)
    checkNewPasswordDifferent(passwordEncoder, currentPassword, newPassword)
}

/**
 * 기존 비밀번호 일치 확인
 */
private fun checkOldPasswordMatches(
    passwordEncoder: PasswordEncoder,
    currentPassword: String,
    oldPassword: String
) {
    if (!passwordEncoder.matches(oldPassword, currentPassword)) {
        throw InvalidRequestException("잘못된 비밀번호입니다.")
    }
}

/**
 * 새 비밀번호가 기존과 다른지 확인
 */
private fun checkNewPasswordDifferent(
    passwordEncoder: PasswordEncoder,
    currentPassword: String,
    newPassword: String
) {
    if (passwordEncoder.matches(newPassword, currentPassword)) {
        throw InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.")
    }
}
