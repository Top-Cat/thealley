package uk.co.thomasc.thealley.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.repo.UserRepository

@Component
class AlleyUserDetails(val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String) =
        userRepository.getUserByName(username).let {
            val userExists = it != null
            object : UserDetails {
                override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
                    return mutableListOf()
                }

                override fun isEnabled() = userExists
                override fun getUsername() = it.username
                override fun isCredentialsNonExpired() = userExists
                override fun getPassword() = it.password
                override fun isAccountNonExpired() = userExists
                override fun isAccountNonLocked() = userExists

            }
        }

}
