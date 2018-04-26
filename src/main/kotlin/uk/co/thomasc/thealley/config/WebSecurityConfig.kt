package uk.co.thomasc.thealley.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.csrf().disable().requestMatchers()
            .requestMatchers(
                OrRequestMatcher(
                    AntPathRequestMatcher("/external/login"),
                    AntPathRequestMatcher("/logout"),
                    AntPathRequestMatcher("/external/oauth/authorize"),
                    AntPathRequestMatcher("/external/oauth/token")
                )
            )
            .and()
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .formLogin().loginPage("/login").loginProcessingUrl("/external/login").permitAll()
            .and()
            .logout().permitAll()
    }

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

}
