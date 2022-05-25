package org.springframework.security.oauth2.common

import java.util.Date

interface ExpiringOAuth2RefreshToken : OAuth2RefreshToken {
    val expiration: Date
}