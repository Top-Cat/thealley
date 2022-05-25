package org.springframework.security.oauth2.provider

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.CredentialsContainer
import org.springframework.security.core.Authentication

class OAuth2Authentication(storedRequest: OAuth2Request, userAuthentication: Authentication?) :
    AbstractAuthenticationToken(if (userAuthentication == null) storedRequest.getAuthorities() else userAuthentication.authorities) {
    private val storedRequest: OAuth2Request

    /**
     * The user authentication.
     *
     * @return The user authentication.
     */
    val userAuthentication: Authentication?

    override val credentials
        get() = ""

    override val principal
        get() = if (userAuthentication == null) storedRequest.clientId else userAuthentication.principal

    /**
     * Convenience method to check if there is a user associated with this token, or just a client application.
     *
     * @return true if this token represents a client app not acting on behalf of a user
     */
    val isClientOnly: Boolean
        get() {
            return userAuthentication == null
        }

    /**
     * The authorization request containing details of the client application.
     *
     * @return The client authentication.
     */
    val oAuth2Request: OAuth2Request
        get() {
            return storedRequest
        }
    override var isAuthenticated: Boolean = false
        get() {
            return (storedRequest.isApproved && (userAuthentication == null || userAuthentication.isAuthenticated))
        }

    override fun eraseCredentials() {
        super.eraseCredentials()
        if (userAuthentication != null && CredentialsContainer::class.java.isAssignableFrom(userAuthentication.javaClass)) {
            CredentialsContainer::class.java.cast(userAuthentication).eraseCredentials()
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (!(o is OAuth2Authentication)) {
            return false
        }
        if (!super.equals(o)) {
            return false
        }
        val that: OAuth2Authentication = o
        if (storedRequest != that.storedRequest) {
            return false
        }
        if (if (userAuthentication != null) !(userAuthentication == that.userAuthentication) else that.userAuthentication != null) {
            return false
        }
        //if (details?.equals(that.details) == false || that.details != null) {
            // return false;
        //}
        return true
    }

    override fun hashCode(): Int {
        var result: Int = super.hashCode()
        result = 31 * result + storedRequest.hashCode()
        result = 31 * result + (userAuthentication?.hashCode() ?: 0)
        return result
    }

    companion object {
        private val serialVersionUID: Long = -4809832298438307309L
    }

    /**
     * Construct an OAuth 2 authentication. Since some grant types don't require user authentication, the user
     * authentication may be null.
     *
     * @param storedRequest The authorization request (must not be null).
     * @param userAuthentication The user authentication (possibly null).
     */
    init {
        this.storedRequest = storedRequest
        this.userAuthentication = userAuthentication
    }
}