package org.springframework.security.oauth2.provider.token

import org.springframework.security.oauth2.provider.OAuth2Authentication

interface AuthenticationKeyGenerator {
    /**
     * @param authentication an OAuth2Authentication
     * @return a unique key identifying the authentication
     */
    fun extractKey(authentication: OAuth2Authentication): String
}