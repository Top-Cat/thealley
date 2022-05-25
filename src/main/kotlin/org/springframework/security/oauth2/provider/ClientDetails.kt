package org.springframework.security.oauth2.provider

import org.springframework.security.core.GrantedAuthority
import java.io.Serializable

interface ClientDetails : Serializable {
    /**
     * The client id.
     *
     * @return The client id.
     */
    val clientId: String

    /**
     * The resources that this client can access. Can be ignored by callers if empty.
     *
     * @return The resources of this client.
     */
    val resourceIds: Set<String>

    /**
     * Whether a secret is required to authenticate this client.
     *
     * @return Whether a secret is required to authenticate this client.
     */
    val isSecretRequired: Boolean

    /**
     * The client secret. Ignored if the [secret isn&#39;t required][.isSecretRequired].
     *
     * @return The client secret.
     */
    val clientSecret: String

    /**
     * Whether this client is limited to a specific scope. If false, the scope of the authentication request will be
     * ignored.
     *
     * @return Whether this client is limited to a specific scope.
     */
    val isScoped: Boolean

    /**
     * The scope of this client. Empty if the client isn't scoped.
     *
     * @return The scope of this client.
     */
    val scope: Set<String>

    /**
     * The grant types for which this client is authorized.
     *
     * @return The grant types for which this client is authorized.
     */
    val authorizedGrantTypes: Set<String>

    /**
     * The pre-defined redirect URI for this client to use during the "authorization_code" access grant. See OAuth spec,
     * section 4.1.1.
     *
     * @return The pre-defined redirect URI for this client.
     */
    val registeredRedirectUri: Set<String>

    /**
     * Returns the authorities that are granted to the OAuth client. Cannot return `null`.
     * Note that these are NOT the authorities that are granted to the user with an authorized access token.
     * Instead, these authorities are inherent to the client itself.
     *
     * @return the authorities (never `null`)
     */
    val authorities: Collection<GrantedAuthority>

    /**
     * The access token validity period for this client. Null if not set explicitly (implementations might use that fact
     * to provide a default value for instance).
     *
     * @return the access token validity period
     */
    val accessTokenValiditySeconds: Int

    /**
     * The refresh token validity period for this client. Null for default value set by token service, and
     * zero or negative for non-expiring tokens.
     *
     * @return the refresh token validity period
     */
    val refreshTokenValiditySeconds: Int

    /**
     * Test whether client needs user approval for a particular scope.
     *
     * @param scope the scope to consider
     * @return true if this client does not need user approval
     */
    fun isAutoApprove(scope: String): Boolean

    /**
     * Additional information for this client, not needed by the vanilla OAuth protocol but might be useful, for example,
     * for storing descriptive information.
     *
     * @return a map of additional information
     */
    val additionalInformation: Map<String, Any>
}