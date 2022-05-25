package org.springframework.security.oauth2.common

import java.util.Date

class DefaultExpiringOAuth2RefreshToken(value: String?, expiration: Date) : DefaultOAuth2RefreshToken(value), ExpiringOAuth2RefreshToken {
    override val expiration: Date

    companion object {
        private const val serialVersionUID = 3449554332764129719L
    }

    /**
     * @param value
     */
    init {
        this.expiration = expiration
    }
}