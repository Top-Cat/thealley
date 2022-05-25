package org.springframework.security.core

import java.io.Serializable
import java.security.Principal

interface Authentication : Principal, Serializable {
    /**
     * Set by an `AuthenticationManager` to indicate the authorities that the
     * principal has been granted. Note that classes should not rely on this value as
     * being valid unless it has been set by a trusted `AuthenticationManager`.
     *
     *
     * Implementations should ensure that modifications to the returned collection array
     * do not affect the state of the Authentication object, or use an unmodifiable
     * instance.
     *
     * @return the authorities granted to the principal, or an empty collection if the
     * token has not been authenticated. Never null.
     */
    val authorities: Collection<GrantedAuthority?>?

    /**
     * The credentials that prove the principal is correct. This is usually a password,
     * but could be anything relevant to the `AuthenticationManager`. Callers
     * are expected to populate the credentials.
     * @return the credentials that prove the identity of the `Principal`
     */
    val credentials: Any?

    /**
     * Stores additional details about the authentication request. These might be an IP
     * address, certificate serial number etc.
     * @return additional details about the authentication request, or `null`
     * if not used
     */
    val details: Any?

    /**
     * The identity of the principal being authenticated. In the case of an authentication
     * request with username and password, this would be the username. Callers are
     * expected to populate the principal for an authentication request.
     *
     *
     * The <tt>AuthenticationManager</tt> implementation will often return an
     * <tt>Authentication</tt> containing richer information as the principal for use by
     * the application. Many of the authentication providers will create a
     * `UserDetails` object as the principal.
     * @return the `Principal` being authenticated or the authenticated
     * principal after authentication.
     */
    val principal: Any?
    /**
     * Used to indicate to `AbstractSecurityInterceptor` whether it should present
     * the authentication token to the `AuthenticationManager`. Typically an
     * `AuthenticationManager` (or, more often, one of its
     * `AuthenticationProvider`s) will return an immutable authentication token
     * after successful authentication, in which case that token can safely return
     * `true` to this method. Returning `true` will improve
     * performance, as calling the `AuthenticationManager` for every request
     * will no longer be necessary.
     *
     *
     * For security reasons, implementations of this interface should be very careful
     * about returning `true` from this method unless they are either
     * immutable, or have some way of ensuring the properties have not been changed since
     * original creation.
     * @return true if the token has been authenticated and the
     * `AbstractSecurityInterceptor` does not need to present the token to the
     * `AuthenticationManager` again for re-authentication.
     */
    /**
     * See [.isAuthenticated] for a full description.
     *
     *
     * Implementations should **always** allow this method to be called with a
     * `false` parameter, as this is used by various classes to specify the
     * authentication token should not be trusted. If an implementation wishes to reject
     * an invocation with a `true` parameter (which would indicate the
     * authentication token is trusted - a potential security risk) the implementation
     * should throw an [IllegalArgumentException].
     * @param isAuthenticated `true` if the token should be trusted (which may
     * result in an exception) or `false` if the token should not be trusted
     * @throws IllegalArgumentException if an attempt to make the authentication token
     * trusted (by passing `true` as the argument) is rejected due to the
     * implementation being immutable or implementing its own alternative approach to
     * [.isAuthenticated]
     */
    @set:Throws(IllegalArgumentException::class)
    var isAuthenticated: Boolean
}