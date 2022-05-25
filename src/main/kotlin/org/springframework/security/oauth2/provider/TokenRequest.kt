package org.springframework.security.oauth2.provider

import org.springframework.security.oauth2.common.util.OAuth2Utils


class TokenRequest : BaseRequest {
    companion object {
        private const val serialVersionUID = -3014451071285659647L
    }

    var grantType: String? = null

    /**
     * Default constructor
     */
    protected constructor() {}

    /**
     * Full constructor. Sets this TokenRequest's requestParameters map to an unmodifiable version of the one provided.
     *
     * @param requestParameters
     * @param clientId
     * @param scope
     * @param grantType
     */
    constructor(
        requestParameters: Map<String, String>?, clientId: String?, scope: Collection<String>?,
        grantType: String?
    ) {
        this.clientId = clientId
        setRequestParameters(requestParameters)
        setScope(scope)
        this.grantType = grantType
    }

    override var clientId: String?
        get() = super.clientId
        public set(clientId) {
            super.clientId = clientId
        }

    /**
     * Set the scope value. If the collection contains only a single scope value, this method will parse that value into
     * a collection using [OAuth2Utils.parseParameterList].
     *
     * @see AuthorizationRequest.setScope
     *
     *
     * @param scope
     */
    public override fun setScope(scope: Collection<String>?) {
        super.setScope(scope)
    }

    /**
     * Set the Request Parameters on this authorization request, which represent the original request parameters and
     * should never be changed during processing. The map passed in is wrapped in an unmodifiable map instance.
     *
     * @see AuthorizationRequest.setRequestParameters
     *
     *
     * @param requestParameters
     */
    public override fun setRequestParameters(requestParameters: Map<String, String>?) {
        super.setRequestParameters(requestParameters)
    }

    fun createOAuth2Request(client: ClientDetails): OAuth2Request {
        val requestParameters: Map<String, String> = getRequestParameters()
        val modifiable = HashMap(requestParameters)
        // Remove password if present to prevent leaks
        modifiable.remove("password")
        modifiable.remove("client_secret")
        // Add grant type so it can be retrieved from OAuth2Request
        modifiable[OAuth2Utils.GRANT_TYPE] = grantType
        return OAuth2Request(
            modifiable, client.clientId, client.authorities, true, getScope(),
            client.resourceIds, null, null, null
        )
    }
}