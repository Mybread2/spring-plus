package org.example.expert.domain.user.service

import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User

interface UserService {

    /**
     * 사용자 엔티티 조회 (다른 도메인에서 연관관계 설정용)
     */
    fun findUserByIdOrThrow(userId: Long): User

    /**
     * 사용자 정보 조회 (DTO 반환)
     */
    fun getUser(userId: Long): UserResponse

    /**
     * 닉네임으로 사용자 검색
     */
    fun searchUsersByNickname(nickname: String, page: Int, size: Int): List<UserResponse>
}
