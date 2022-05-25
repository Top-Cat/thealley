package org.springframework.security.oauth2.common

import java.util.Date

interface OAuth2AccessToken {
    /**
     * The additionalInformation map is used by the token serializers to export any fields used by extensions of OAuth.
     * @return a map from the field name in the serialized token to the value to be exported. The default serializers
     * make use of Jackson's automatic JSON mapping for Java objects (for the Token Endpoint flows) or implicitly call
     * .toString() on the "value" object (for the implicit flow) as part of the serialization process.
     */
    val additionalInformation: Map<String?, Any?>?
    val scope: Set<String?>?
    val refreshToken: OAuth2RefreshToken?
    val tokenType: String?
    val isExpired: Boolean
    val expiration: Date?
    val expiresIn: Int
    val value: String?

    companion object {
        const val BEARER_TYPE = "Bearer"
        const val OAUTH2_TYPE = "OAuth2"

        /**
         * The access token issued by the authorization server. This value is REQUIRED.
         */
        const val ACCESS_TOKEN = "access_token"

        /**
         * The type of the token issued as described in [Section 7.1](https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-7.1). Value is case insensitive.
         * This value is REQUIRED.
         */
        const val TOKEN_TYPE = "token_type"

        /**
         * The lifetime in seconds of the access token. For example, the value "3600" denotes that the access token will
         * expire in one hour from the time the response was generated. This value is OPTIONAL.
         */
        const val EXPIRES_IN = "expires_in"

        /**
         * The refresh token which can be used to obtain new access tokens using the same authorization grant as described
         * in [Section 6](https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-6). This value is OPTIONAL.
         */
        const val REFRESH_TOKEN = "refresh_token"

        /**
         * The scope of the access token as described by [Section 3.3](https://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-3.3)
         */
        const val SCOPE = "scope"
    }
}