package org.springframework.security.web.authentication.preauth

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority


class PreAuthenticatedAuthenticationToken : AbstractAuthenticationToken {
    /**
     * Get the principal
     */
    override val principal: Any

    /**
     * Get the credentials
     */
    override val credentials: Any

    /**
     * Constructor used for an authentication request. The
     * [org.springframework.security.core.Authentication.isAuthenticated] will
     * return `false`.
     * @param aPrincipal The pre-authenticated principal
     * @param aCredentials The pre-authenticated credentials
     */
    constructor(aPrincipal: Any, aCredentials: Any) : super(null) {
        principal = aPrincipal
        credentials = aCredentials
    }

    /**
     * Constructor used for an authentication response. The
     * [org.springframework.security.core.Authentication.isAuthenticated] will
     * return `true`.
     * @param aPrincipal The authenticated principal
     * @param anAuthorities The granted authorities
     */
    constructor(
        aPrincipal: Any, aCredentials: Any,
        anAuthorities: Collection<GrantedAuthority?>?
    ) : super(anAuthorities) {
        principal = aPrincipal
        credentials = aCredentials
        isAuthenticated = true
    }

    companion object {
        private val serialVersionUID: Long = 500L
    }
}