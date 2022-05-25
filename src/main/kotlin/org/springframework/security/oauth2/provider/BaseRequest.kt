package org.springframework.security.oauth2.provider

import org.springframework.security.oauth2.common.util.OAuth2Utils
import java.io.Serializable
import java.util.Collections


abstract class BaseRequest : Serializable {
    companion object {
        private const val serialVersionUID = 3902503486565214653L
    }

    /**
     * Resolved client ID. This may be present in the original request
     * parameters, or in some cases may be inferred by a processing class and
     * inserted here.
     */
    open var clientId: String? = null
        protected set

    /**
     * Resolved scope set, initialized (by the OAuth2RequestFactory) with the
     * scopes originally requested. Further processing and user interaction may
     * alter the set of scopes that is finally granted and stored when the
     * request processing is complete.
     */
    private var scope: Set<String>? = HashSet()

    /**
     * Map of parameters passed in to the Authorization Endpoint or Token
     * Endpoint, preserved unchanged from the original request. This map should
     * not be modified after initialization. In general, classes should not
     * retrieve values from this map directly, and should instead use the
     * individual members on this class.
     *
     * The OAuth2RequestFactory is responsible for initializing all members of
     * this class, usually by parsing the values inside the requestParmaeters
     * map.
     *
     */
    private var requestParameters: Map<String, String> = Collections
        .unmodifiableMap(HashMap<String, String>())

    fun getScope(): Set<String>? {
        return scope
    }

    /**
     * Warning: most clients should use the individual properties of this class,
     * such as {[.getScope] or { [.getClientId], rather than
     * retrieving values from this map.
     *
     * @return the original, unchanged set of request parameters
     */
    fun getRequestParameters(): Map<String, String> {
        return requestParameters
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = (prime * result
                + if (clientId == null) 0 else clientId.hashCode())
        result = (prime
                * result
                + if (requestParameters == null) 0 else requestParameters
            .hashCode())
        result = prime * result + if (scope == null) 0 else scope.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null) return false
        if (javaClass != obj.javaClass) return false
        val other = obj as BaseRequest
        if (clientId == null) {
            if (other.clientId != null) return false
        } else if (clientId != other.clientId) return false
        if (requestParameters == null) {
            if (other.requestParameters != null) return false
        } else if (requestParameters != other.requestParameters) return false
        if (scope == null) {
            if (other.scope != null) return false
        } else if (scope != other.scope) return false
        return true
    }

    protected open fun setScope(scope: Collection<String>?) {
        var scope = scope
        if (scope != null && scope.size == 1) {
            val value = scope.iterator().next()
            /*
			 * This is really an error, but it can catch out unsuspecting users
			 * and it's easy to fix. It happens when an AuthorizationRequest
			 * gets bound accidentally from request parameters using
			 * @ModelAttribute.
			 */if (value.contains(" ") || value.contains(",")) {
                scope = OAuth2Utils.parseParameterList(value)
            }
        }
        this.scope = Collections
            .unmodifiableSet(scope?.let { LinkedHashSet(it) } ?: LinkedHashSet())
    }

    protected open fun setRequestParameters(requestParameters: Map<String, String>?) {
        if (requestParameters != null) {
            this.requestParameters = Collections
                .unmodifiableMap(HashMap(requestParameters))
        }
    }
}