package org.springframework.security.oauth2.provider

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.util.OAuth2Utils
import java.io.Serializable


class OAuth2Request : BaseRequest, Serializable {
	/**
	 * Resolved resource IDs. This set may change during request processing.
	 */
	var resourceIds: Set<String>? = hashSetOf()
		private set

	/**
	 * Resolved granted authorities for this request. May change during request processing.
	 */
	private var authorities: Collection<GrantedAuthority>? = hashSetOf()

	/**
	 * Whether the request has been approved by the end user (or other process). This will be altered by the User
	 * Approval Endpoint and/or the UserApprovalHandler as appropriate.
	 */
	var isApproved = false
		private set

	/**
	 * Will be non-null if the request is for a token to be refreshed (the original grant type might still be available
	 * via [.getGrantType]).
	 */
	private var refresh: TokenRequest? = null

	/**
	 * The resolved redirect URI of this request. A URI may be present in the original request, in the
	 * authorizationParameters, or it may not be provided, in which case it will be defaulted (by processing classes) to
	 * the Client's default registered value.
	 */
	var redirectUri: String? = null
		private set

	/**
	 * Resolved requested response types initialized (by the OAuth2RequestFactory) with the response types originally
	 * requested.
	 */
	var responseTypes: Set<String>? = HashSet()
		private set

	/**
	 * Extension point for custom processing classes which may wish to store additional information about the OAuth2
	 * request. Since this class is serializable, all members of this map must also be serializable.
	 */
	var extensions: Map<String, Serializable>? = HashMap()
		private set

	constructor(
		requestParameters: Map<String, String>, clientId: String?,
		authorities: Collection<GrantedAuthority>?, approved: Boolean, scope: Set<String>?,
		resourceIds: Set<String>?, redirectUri: String?, responseTypes: Set<String>?,
		extensionProperties: Map<String, Serializable>?
	) {
		this.clientId = clientId
		setRequestParameters(requestParameters)
		setScope(scope)
		if (resourceIds != null) {
			this.resourceIds = HashSet(resourceIds)
		}
		if (authorities != null) {
			this.authorities = HashSet<GrantedAuthority>(authorities)
		}
		isApproved = approved
		if (responseTypes != null) {
			this.responseTypes = HashSet(responseTypes)
		}
		this.redirectUri = redirectUri
		if (extensionProperties != null) {
			extensions = extensionProperties
		}
	}

	protected constructor(other: OAuth2Request) : this(
		other.getRequestParameters(), other.clientId, other.authorities, other.isApproved, other
			.getScope(), other.resourceIds, other.redirectUri, other.responseTypes, other
			.extensions
	)

	protected constructor(clientId: String?) {
		this.clientId = clientId
	}

	protected constructor() : super()

	fun getAuthorities(): Collection<GrantedAuthority?>? {
		return authorities
	}

	/**
	 * Update the request parameters and return a new object with the same properties except the parameters.
	 * @param parameters new parameters replacing the existing ones
	 * @return a new OAuth2Request
	 */
	fun createOAuth2Request(parameters: Map<String, String>): OAuth2Request {
		return OAuth2Request(
			parameters, clientId, authorities, isApproved, getScope(), resourceIds,
			redirectUri, responseTypes, extensions
		)
	}

	/**
	 * Update the scope and create a new request. All the other properties are the same (including the request
	 * parameters).
	 *
	 * @param scope the new scope
	 * @return a new request with the narrowed scope
	 */
	fun narrowScope(scope: Set<String>): OAuth2Request {
		val request = OAuth2Request(
			getRequestParameters(), clientId, authorities, isApproved, scope,
			resourceIds, redirectUri, responseTypes, extensions
		)
		request.refresh = refresh
		return request
	}

	fun refresh(tokenRequest: TokenRequest?): OAuth2Request {
		val request = OAuth2Request(
			getRequestParameters(), clientId, authorities, isApproved,
			getScope(), resourceIds, redirectUri, responseTypes, extensions
		)
		request.refresh = tokenRequest
		return request
	}

	/**
	 * @return true if this request is known to be for a token to be refreshed
	 */
	fun isRefresh(): Boolean {
		return refresh != null
	}

	/**
	 * If this request was for an access token to be refreshed, then the [TokenRequest] that led to the refresh
	 * *may* be available here if it is known.
	 *
	 * @return the refresh token request (may be null)
	 */
	val refreshTokenRequest: TokenRequest?
		get() = refresh

	/**
	 * Tries to discover the grant type requested for the token associated with this request.
	 *
	 * @return the grant type if known, or null otherwise
	 */
	val grantType: String?
		get() {
			if (getRequestParameters().containsKey(OAuth2Utils.GRANT_TYPE)) {
				return getRequestParameters()[OAuth2Utils.GRANT_TYPE]
			}
			if (getRequestParameters().containsKey(OAuth2Utils.RESPONSE_TYPE)) {
				val response: String = getRequestParameters()[OAuth2Utils.RESPONSE_TYPE] ?: ""
				if (response.contains("token")) {
					return "implicit"
				}
			}
			return null
		}

	override fun hashCode(): Int {
		val prime = 31
		var result = super.hashCode()
		result = prime * result + if (isApproved) 1231 else 1237
		result = prime * result + if (authorities == null) 0 else authorities.hashCode()
		result = prime * result + if (extensions == null) 0 else extensions.hashCode()
		result = prime * result + if (redirectUri == null) 0 else redirectUri.hashCode()
		result = prime * result + if (resourceIds == null) 0 else resourceIds.hashCode()
		result = prime * result + if (responseTypes == null) 0 else responseTypes.hashCode()
		return result
	}

	override fun equals(obj: Any?): Boolean {
		if (this === obj) return true
		if (!super.equals(obj)) return false
		if (javaClass != obj.javaClass) return false
		val other = obj as OAuth2Request
		if (isApproved != other.isApproved) return false
		if (authorities == null) {
			if (other.authorities != null) return false
		} else if (authorities != other.authorities) return false
		if (extensions == null) {
			if (other.extensions != null) return false
		} else if (extensions != other.extensions) return false
		if (redirectUri == null) {
			if (other.redirectUri != null) return false
		} else if (redirectUri != other.redirectUri) return false
		if (resourceIds == null) {
			if (other.resourceIds != null) return false
		} else if (resourceIds != other.resourceIds) return false
		if (responseTypes == null) {
			if (other.responseTypes != null) return false
		} else if (responseTypes != other.responseTypes) return false
		return true
	}

	companion object {
		private const val serialVersionUID = 1L
	}
}