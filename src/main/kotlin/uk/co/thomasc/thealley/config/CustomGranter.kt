package uk.co.thomasc.thealley.config

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2RequestFactory
import org.springframework.security.oauth2.provider.TokenRequest
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices

class CustomGranter(tokenServices: AuthorizationServerTokenServices, clientDetailsService: ClientDetailsService, requestFactory: OAuth2RequestFactory, grantType: String) : AbstractTokenGranter(tokenServices, clientDetailsService, requestFactory, grantType) {

    override fun getOAuth2Authentication(client: ClientDetails, tokenRequest: TokenRequest): OAuth2Authentication {
        println("STEVESTEVESTEVE")
        val params = tokenRequest.requestParameters

        val authorities: List<GrantedAuthority> = if (params.containsKey("authorities")) {
            AuthorityUtils.createAuthorityList(*(OAuth2Utils.parseParameterList(params["authorities"])).toTypedArray())
        } else {
            AuthorityUtils.NO_AUTHORITIES
        }

        val username = tokenRequest.requestParameters["username"] ?: "guest"

        val user = UsernamePasswordAuthenticationToken(username, "N/A", authorities)

        return OAuth2Authentication(tokenRequest.createOAuth2Request(client), user)
    }

}
