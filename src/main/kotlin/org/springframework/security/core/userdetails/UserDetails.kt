package org.springframework.security.core.userdetails

import org.springframework.security.core.GrantedAuthority

import java.io.Serializable


interface UserDetails : Serializable {
    /**
     * Returns the authorities granted to the user. Cannot return `null`.
     * @return the authorities, sorted by natural key (never `null`)
     */
    val authorities: Collection<GrantedAuthority?>?

    /**
     * Returns the password used to authenticate the user.
     * @return the password
     */
    val password: String?

    /**
     * Returns the username used to authenticate the user. Cannot return
     * `null`.
     * @return the username (never `null`)
     */
    val username: String

    /**
     * Indicates whether the user's account has expired. An expired account cannot be
     * authenticated.
     * @return `true` if the user's account is valid (ie non-expired),
     * `false` if no longer valid (ie expired)
     */
    val isAccountNonExpired: Boolean

    /**
     * Indicates whether the user is locked or unlocked. A locked user cannot be
     * authenticated.
     * @return `true` if the user is not locked, `false` otherwise
     */
    val isAccountNonLocked: Boolean

    /**
     * Indicates whether the user's credentials (password) has expired. Expired
     * credentials prevent authentication.
     * @return `true` if the user's credentials are valid (ie non-expired),
     * `false` if no longer valid (ie expired)
     */
    val isCredentialsNonExpired: Boolean

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     * @return `true` if the user is enabled, `false` otherwise
     */
    val isEnabled: Boolean
}
