package org.springframework.security.authentication

import org.springframework.security.core.GrantedAuthority


class UsernamePasswordAuthenticationToken : AbstractAuthenticationToken {
    override val principal: Any
    override var credentials: Any?
        private set

    /**
     * This constructor can be safely used by any code that wishes to create a
     * `UsernamePasswordAuthenticationToken`, as the [.isAuthenticated]
     * will return `false`.
     *
     */
    constructor(principal: Any, credentials: Any?) : super(null) {
        this.principal = principal
        this.credentials = credentials
        isAuthenticated = false
    }

    /**
     * This constructor should only be used by `AuthenticationManager` or
     * `AuthenticationProvider` implementations that are satisfied with
     * producing a trusted (i.e. [.isAuthenticated] = `true`)
     * authentication token.
     * @param principal
     * @param credentials
     * @param authorities
     */
    constructor(
        principal: Any, credentials: Any?,
        authorities: Collection<GrantedAuthority?>?
    ) : super(authorities) {
        this.principal = principal
        this.credentials = credentials
        super.isAuthenticated = true // must use super, as we override
    }

    override var isAuthenticated: Boolean
        get() = super.isAuthenticated
        set(value) {
            assert(!value) {
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead"
            }
            super.isAuthenticated = false
        }

    override fun eraseCredentials() {
        super.eraseCredentials()
        credentials = null
    }

    companion object {
        private val serialVersionUID: Long = 500L

        /**
         * This factory method can be safely used by any code that wishes to create a
         * unauthenticated `UsernamePasswordAuthenticationToken`.
         * @param principal
         * @param credentials
         * @return UsernamePasswordAuthenticationToken with false isAuthenticated() result
         *
         * @since 5.7
         */
        fun unauthenticated(principal: Any, credentials: Any?): UsernamePasswordAuthenticationToken {
            return UsernamePasswordAuthenticationToken(principal, credentials)
        }

        /**
         * This factory method can be safely used by any code that wishes to create a
         * authenticated `UsernamePasswordAuthenticationToken`.
         * @param principal
         * @param credentials
         * @return UsernamePasswordAuthenticationToken with true isAuthenticated() result
         *
         * @since 5.7
         */
        fun authenticated(
            principal: Any, credentials: Any?,
            authorities: Collection<GrantedAuthority?>?
        ): UsernamePasswordAuthenticationToken {
            return UsernamePasswordAuthenticationToken(principal, credentials, authorities)
        }
    }
}