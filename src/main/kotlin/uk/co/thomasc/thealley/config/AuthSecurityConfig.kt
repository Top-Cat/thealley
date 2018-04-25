package uk.co.thomasc.thealley.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.CompositeTokenGranter
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import javax.sql.DataSource

@EnableAuthorizationServer
@Configuration
class AuthSecurityConfig(
    val db: DataSource,
    val authenticationManagerBean: AuthenticationManager,
    val clientProperties: ClientProperties
) : AuthorizationServerConfigurerAdapter() {

    @Bean
    fun tokenStore() = JdbcTokenStore(db)

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    fun tokenGranter(endpoints: AuthorizationServerEndpointsConfigurer): CompositeTokenGranter {
        return CompositeTokenGranter(arrayListOf(endpoints.tokenGranter,
            CustomGranter(endpoints.tokenServices, endpoints.clientDetailsService, endpoints.oAuth2RequestFactory, "custom")
        ))
    }

    @Bean
    fun tokenServices() = DefaultTokenServices().apply {
        setTokenStore(tokenStore())
        setSupportRefreshToken(true)
    }

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        val db = clients.inMemory()
        clientProperties.clients.forEach {
            db.withClient(it.clientId)
                .secret(it.secret)
                .scopes(*it.scopes)
                .authorizedGrantTypes("authorization_code", "refresh_token")
        }
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints
            .pathMapping("/oauth/authorize","/external/oauth/authorize")
            .pathMapping("/oauth/token","/external/oauth/token")
            .tokenStore(tokenStore())
            .authenticationManager(authenticationManagerBean)
            .tokenGranter(tokenGranter(endpoints))
    }

    override fun configure(security: AuthorizationServerSecurityConfigurer) {
        // this means anyone can check if a token is valid or not and get the scope and client id
        // change it to isAuthenticated() to require clientid/secret with basic auth
        security.checkTokenAccess("permitAll()")
        // this lets anyone get the public key for our JWT's
        security.tokenKeyAccess("permitAll()")
        security.allowFormAuthenticationForClients()
    }

}
