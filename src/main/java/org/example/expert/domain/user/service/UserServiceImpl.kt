package org.example.expert.domain.user.service

import org.example.expert.domain.common.exception.InvalidRequestException
import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User
import org.example.expert.domain.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    /**
     * 헬퍼 메서드 - 사용자 조회 (예외 처리 포함)
     */
    override fun findUserByIdOrThrow(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { InvalidRequestException("사용자를 찾을 수 없습니다. ID: $userId") }
    }

    // === 비즈니스 메서드들 ===

    override fun getUser(userId: Long): UserResponse {
        val user = findUserByIdOrThrow(userId)

        return UserResponse(
            id = user.id!!,
            username = user.userName,
            email = user.email
        )
    }

    override fun searchUsersByNickname(nickname: String, page: Int, size: Int): List<UserResponse> {
        val pageable = PageRequest.of(page, size)
        val users = userRepository.findByUserNameExact(nickname, pageable)

        return users.content.map { user ->
            UserResponse(
                id = user.id!!,
                username = user.userName,
                email = user.email
            )
        }
    }
}

