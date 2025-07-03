package org.example.expert.domain.user.repository

import org.example.expert.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    fun existsByEmail(email: String): Boolean

    @Query("SELECT u FROM User u WHERE u.userName = :nickname")
    fun findByUserNameExact(@Param("nickname") nickname: String, pageable: Pageable): Page<User>
}
