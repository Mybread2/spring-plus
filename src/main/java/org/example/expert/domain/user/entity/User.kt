package org.example.expert.domain.user.entity

import jakarta.persistence.*
import org.example.expert.domain.common.entity.Timestamped
import org.example.expert.domain.user.enums.UserRole

@Entity
@Table(name = "users")
open class User (

    @Column(unique = true)
    var email : String,

    var userName : String,

    var password : String,

    @Enumerated(EnumType.STRING)
    var userRole : UserRole,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long? = null
) : Timestamped() {

    protected constructor() : this("", "", "", UserRole.USER, null)

    fun changePassword(
        oldPassword: String,
        newPassword: String,
    ) {
        checkPassword(this.password, oldPassword, newPassword)
        this.password = newPassword
    }

    fun changeRole(newRole: String) {
        this.userRole = UserRole.of(newRole)
    }

    override fun toString(): String {
        return "User(id=$id, email='$email', userName='$userName', userRole=$userRole)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
