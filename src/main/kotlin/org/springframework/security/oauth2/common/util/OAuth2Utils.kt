package org.springframework.security.oauth2.common.util

import java.util.Arrays
import java.util.TreeSet

object OAuth2Utils {
    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val CLIENT_ID = "client_id"

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val STATE = "state"

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val SCOPE = "scope"

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val REDIRECT_URI = "redirect_uri"

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val RESPONSE_TYPE = "response_type"

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val USER_OAUTH_APPROVAL = "user_oauth_approval"

    /**
     * Constant to use as a prefix for scope approval
     */
    const val SCOPE_PREFIX = "scope."

    /**
     * Constant to use while parsing and formatting parameter maps for OAuth2 requests
     */
    const val GRANT_TYPE = "grant_type"

    /**
     * Parses a string parameter value into a set of strings.
     *
     * @param values The values of the set.
     * @return The set.
     */
    fun parseParameterList(values: String?): Set<String> {
        val result: MutableSet<String> = TreeSet()
        if (values != null && values.trim { it <= ' ' }.length > 0) {
            // the spec says the scope is separated by spaces
            val tokens = values.split("[\\s+]".toRegex()).toTypedArray()
            result.addAll(Arrays.asList(*tokens))
        }
        return result
    }

    fun formatParameterList(value: Collection<String?>?): String? {
        return value?.joinToString(" ")
    }

    /**
     * Compare 2 sets and check that one contains all members of the other.
     *
     * @param target set of strings to check
     * @param members the members to compare to
     * @return true if all members are in the target
     */
    fun containsAll(target: MutableSet<String?>, members: Set<String?>): Boolean {
        var target = target
        target = HashSet(target)
        target.retainAll(members)
        return target.size == members.size
    }
}