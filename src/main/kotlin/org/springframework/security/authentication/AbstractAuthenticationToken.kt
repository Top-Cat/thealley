package org.springframework.security.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

abstract class AbstractAuthenticationToken(authorities: Collection<GrantedAuthority?>?) : Authentication, CredentialsContainer {
    final override val authorities: Collection<GrantedAuthority>
    override var details: Any? = null
    override var isAuthenticated = false

    override fun getName(): String =
        principal.let { p ->
            when (p) {
                is Principal -> p.name
                is UserDetails -> p.username
                //is AuthenticatedPrincipal -> p.getName()
                else -> p?.toString() ?: ""
            }
        }

    companion object {
        private const val serialVersionUID = -3194696462184782834L
    }

    /**
     * Checks the `credentials`, `principal` and `details` objects,
     * invoking the `eraseCredentials` method on any which implement
     * [CredentialsContainer].
     */
    override fun eraseCredentials() {
        eraseSecret(credentials)
        eraseSecret(principal)
        eraseSecret(details)
    }

    private fun eraseSecret(secret: Any?) {
        if (secret is CredentialsContainer) {
            secret.eraseCredentials()
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is AbstractAuthenticationToken) {
            return false
        }
        val test = obj
        if (authorities != test.authorities) {
            return false
        }
        if (details == null && test.details != null) {
            return false
        }
        if (details != null && test.details == null) {
            return false
        }
        if (details != null && details != test.details) {
            return false
        }
        if (this.credentials == null && test.credentials != null) {
            return false
        }
        if (this.credentials?.equals(test.credentials) == false) {
            return false
        }
        if (this.principal == null && test.principal != null) {
            return false
        }
        return if (this.principal?.equals(test.principal) == false) {
            false
        } else isAuthenticated == test.isAuthenticated
    }

    override fun hashCode(): Int {
        var code = 31
        for (authority in authorities) {
            code = code xor authority.hashCode()
        }
        if (this.principal != null) {
            code = code xor this.principal.hashCode()
        }
        if (this.credentials != null) {
            code = code xor this.credentials.hashCode()
        }
        if (details != null) {
            code = code xor details.hashCode()
        }
        if (isAuthenticated) {
            code = code xor -37
        }
        return code
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(javaClass.simpleName).append(" [")
        sb.append("Principal=").append(principal).append(", ")
        sb.append("Credentials=[PROTECTED], ")
        sb.append("Authenticated=").append(isAuthenticated).append(", ")
        sb.append("Details=").append(details).append(", ")
        sb.append("Granted Authorities=").append(authorities)
        sb.append("]")
        return sb.toString()
    }

    /**
     * Creates a token with the supplied array of authorities.
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     * represented by this authentication object.
     */
    init {
        if (authorities == null) {
            this.authorities = AuthorityUtils.NO_AUTHORITIES
        } else {
            for (a in authorities) {
                assert(a != null) {
                    "Authorities collection cannot contain any null elements"
                }
            }
            this.authorities = authorities.filterNotNull()
        }
    }
}