package org.springframework.security.oauth2.common

import com.fasterxml.jackson.annotation.JsonValue

interface OAuth2RefreshToken {
    /**
     * The value of the token.
     *
     * @return The value of the token.
     */
    @get:JsonValue
    val value: String?
}