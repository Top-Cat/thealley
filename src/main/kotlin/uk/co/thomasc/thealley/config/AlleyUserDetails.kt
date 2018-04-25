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
            AlleyUser(it != null, it?.username, it?.password)
        }

}

class AlleyUser(private val userExists: Boolean, private val user: String?, private val pass: String?) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }

    override fun isEnabled() = userExists
    override fun getUsername() = user
    override fun isCredentialsNonExpired() = userExists
    override fun getPassword() = pass
    override fun isAccountNonExpired() = userExists
    override fun isAccountNonLocked() = userExists

}
