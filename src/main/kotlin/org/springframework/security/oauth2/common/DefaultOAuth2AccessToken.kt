package org.springframework.security.oauth2.common

import java.io.Serializable
import java.util.Date
import java.util.StringTokenizer
import java.util.TreeSet

class DefaultOAuth2AccessToken
/**
 * Create an access token from the value provided.
 */(
    /**
     * The token value.
     *
     * @return The token value.
     */
    override var value: String?
) : Serializable, OAuth2AccessToken {
    override var expiration: Date? = null
    /**
     * The token type, as introduced in draft 11 of the OAuth 2 spec. The spec doesn't define (yet) that the valid token
     * types are, but says it's required so the default will just be "undefined".
     *
     * @return The token type, as introduced in draft 11 of the OAuth 2 spec.
     */
    /**
     * The token type, as introduced in draft 11 of the OAuth 2 spec.
     *
     * @param tokenType The token type, as introduced in draft 11 of the OAuth 2 spec.
     */
    override var tokenType: String? = OAuth2AccessToken.BEARER_TYPE.lowercase()
    /**
     * The refresh token associated with the access token, if any.
     *
     * @return The refresh token associated with the access token, if any.
     */
    /**
     * The refresh token associated with the access token, if any.
     *
     * @param refreshToken The refresh token associated with the access token, if any.
     */
    override var refreshToken: OAuth2RefreshToken? = null
    /**
     * The scope of the token.
     *
     * @return The scope of the token.
     */
    /**
     * The scope of the token.
     *
     * @param scope The scope of the token.
     */
    override var scope: Set<String?>? = null
    /**
     * Additional information that token granters would like to add to the token, e.g. to support new token types.
     *
     * @return the additional information (default empty)
     */
    /**
     * Additional information that token granters would like to add to the token, e.g. to support new token types. If
     * the values in the map are primitive then remote communication is going to always work. It should also be safe to
     * use maps (nested if desired), or something that is explicitly serializable by Jackson.
     *
     * @param additionalInformation the additional information to set
     */
    override var additionalInformation: Map<String?, Any?> = emptyMap()
        set(additionalInformation) {
            field = LinkedHashMap(additionalInformation)
        }

    /**
     * Private constructor for JPA and other serialization tools.
     */
    private constructor() : this(null as String?) {}

    /**
     * Copy constructor for access token.
     *
     * @param accessToken
     */
    constructor(accessToken: OAuth2AccessToken) : this(accessToken.value) {
        additionalInformation = accessToken.additionalInformation!!
        refreshToken = accessToken.refreshToken
        expiration = accessToken.expiration
        scope = accessToken.scope
        tokenType = accessToken.tokenType
    }

    override var expiresIn: Int
        get() = expiration?.let { java.lang.Long.valueOf((it.time - System.currentTimeMillis()) / 1000L).toInt() } ?: 0
        protected set(delta) {
            expiration = Date(System.currentTimeMillis() + delta)
        }

    /**
     * Convenience method for checking expiration
     *
     * @return true if the expiration is befor ethe current time
     */
    override val isExpired: Boolean
        get() = expiration?.before(Date()) ?: false

    override fun equals(obj: Any?): Boolean {
        return obj != null && toString() == obj.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String {
        return value.toString()
    }

    companion object {
        private const val serialVersionUID = 914967629530462926L
        fun valueOf(tokenParams: Map<String?, String>): OAuth2AccessToken {
            val token = DefaultOAuth2AccessToken(tokenParams[OAuth2AccessToken.ACCESS_TOKEN])
            if (tokenParams.containsKey(OAuth2AccessToken.EXPIRES_IN)) {
                var expiration: Long = 0
                try {
                    expiration = tokenParams[OAuth2AccessToken.EXPIRES_IN].toString().toLong()
                } catch (e: NumberFormatException) {
                    // fall through...
                }
                token.expiration = Date(System.currentTimeMillis() + expiration * 1000L)
            }
            if (tokenParams.containsKey(OAuth2AccessToken.REFRESH_TOKEN)) {
                val refresh = tokenParams[OAuth2AccessToken.REFRESH_TOKEN]
                val refreshToken = DefaultOAuth2RefreshToken(refresh)
                token.refreshToken = refreshToken
            }
            if (tokenParams.containsKey(OAuth2AccessToken.SCOPE)) {
                val scope: MutableSet<String?> = TreeSet()
                val tokenizer = StringTokenizer(tokenParams[OAuth2AccessToken.SCOPE], " ,")
                while (tokenizer
                        .hasMoreTokens()
                ) {
                    scope.add(tokenizer.nextToken())
                }
                token.scope = scope
            }
            if (tokenParams.containsKey(OAuth2AccessToken.TOKEN_TYPE)) {
                token.tokenType = tokenParams[OAuth2AccessToken.TOKEN_TYPE]
            }
            return token
        }
    }
}