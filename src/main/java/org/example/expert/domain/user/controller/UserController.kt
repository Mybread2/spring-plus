package org.example.expert.domain.user.controller

import jakarta.validation.Valid
import org.example.expert.domain.common.annotation.Auth
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest
import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/users/{userId}")
    fun getUser(@PathVariable userId: Long): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUser(userId))
    }

    @PutMapping("/users")
    fun changePassword(
        @Auth authUser: AuthUser,
        @Valid @RequestBody userChangePasswordRequest: UserChangePasswordRequest
    ) {
        val user = userService.findUserByIdOrThrow(authUser.id)

        user.changePassword(
            oldPassword = userChangePasswordRequest.oldPassword,
            newPassword = userChangePasswordRequest.newPassword
        )
    }

    @GetMapping("/users/search")
    fun searchUsersByNickName(
        @RequestParam nickname: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<UserResponse>> {
        val users = userService.searchUsersByNickname(nickname, page, size)
        return ResponseEntity.ok(users)
    }

    @PatchMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    fun changeUserRole(
        @PathVariable userId: Long,
        @Valid @RequestBody request: UserRoleChangeRequest
    ) {
        val user = userService.findUserByIdOrThrow(userId)

        user.changeRole(request.role)
    }
}
