package org.springframework.security.oauth2.provider.token

import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.provider.OAuth2Authentication
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.TreeSet

class DefaultAuthenticationKeyGenerator : AuthenticationKeyGenerator {
    override fun extractKey(authentication: OAuth2Authentication): String {
        val values: MutableMap<String, String?> = LinkedHashMap()
        val authorizationRequest = authentication.oAuth2Request
        if (!authentication.isClientOnly) {
            values[USERNAME] = authentication.name
        }
        values[CLIENT_ID] = authorizationRequest.clientId
        if (authorizationRequest.getScope() != null) {
            values[SCOPE] = OAuth2Utils.formatParameterList(TreeSet(authorizationRequest.getScope()))
        }
        return generateKey(values)
    }

    protected fun generateKey(values: Map<String, String?>): String {
        val digest: MessageDigest
        return try {
            digest = MessageDigest.getInstance("MD5")
            val bytes = digest.digest(values.toString().toByteArray(charset("UTF-8")))
            String.format("%032x", BigInteger(1, bytes))
        } catch (nsae: NoSuchAlgorithmException) {
            throw IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).", nsae)
        } catch (uee: UnsupportedEncodingException) {
            throw IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).", uee)
        }
    }

    companion object {
        private const val CLIENT_ID = "client_id"
        private const val SCOPE = "scope"
        private const val USERNAME = "username"
    }
}