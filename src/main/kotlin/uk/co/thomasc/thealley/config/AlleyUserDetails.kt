package uk.co.thomasc.thealley.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AlleyUser(private val userExists: Boolean, private val user: String, private val pass: String?) : UserDetails {
    override val authorities: Collection<GrantedAuthority?>
        get() = mutableListOf()

    override val isAccountNonExpired: Boolean
        get() = userExists

    override val isCredentialsNonExpired: Boolean
        get() = userExists

    override val isAccountNonLocked: Boolean
        get() = userExists

    override val isEnabled: Boolean
        get() = userExists

    override val username: String
        get() = user

    override val password: String?
        get() = pass
}
