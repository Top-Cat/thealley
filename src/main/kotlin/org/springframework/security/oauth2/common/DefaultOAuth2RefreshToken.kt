package org.springframework.security.oauth2.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

import java.io.Serializable

open class DefaultOAuth2RefreshToken
/**
 * Create a new refresh token.
 */ @JsonCreator constructor(
    @get:JsonValue override val value: String?
) : Serializable, OAuth2RefreshToken {

    /**
     * Default constructor for JPA and other serialization tools.
     */
    private constructor() : this(null) {}

    override fun toString(): String {
        return value ?: "null"
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is DefaultOAuth2RefreshToken) {
            return false
        }
        return !if (value != null) value != o.value else o.value != null
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    companion object {
        private const val serialVersionUID = 8349970621900575838L
    }
}