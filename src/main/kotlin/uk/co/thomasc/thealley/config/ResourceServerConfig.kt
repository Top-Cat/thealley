package uk.co.thomasc.thealley.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer
import org.springframework.security.oauth2.provider.approval.ApprovalStore
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import javax.sql.DataSource

@Configuration
@EnableResourceServer
class ResourceServerConfig(
    val db: DataSource
) : ResourceServerConfigurerAdapter() {

    @Bean
    fun tokenStore() = JdbcTokenStore(db)

    @Bean
    @Primary
    fun tokenServices() = DefaultTokenServices().apply {
        setTokenStore(tokenStore())
        setSupportRefreshToken(true)
    }

    @Bean
    @Throws(Exception::class)
    fun approvalStore(): ApprovalStore {
        val store = TokenApprovalStore()
        store.setTokenStore(tokenStore())
        return store
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .requestMatchers()
            .requestMatchers(
                NegatedRequestMatcher(
                    OrRequestMatcher(
                        AntPathRequestMatcher("/external/login"),
                        AntPathRequestMatcher("/logout"),
                        AntPathRequestMatcher("/external/oauth/authorize"),
                        AntPathRequestMatcher("/external/oauth/token")
                    )
                )
            )
            .and().authorizeRequests()
            .antMatchers("/external/googlehome").access("#oauth2.hasScope('read')")
            .anyRequest().permitAll()
    }

    override fun configure(resources: ResourceServerSecurityConfigurer) {
        resources
            .tokenStore(tokenStore())
            .tokenServices(tokenServices())
    }
}
