package uk.co.thomasc.thealley.repo

import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
@DependsOn("flywayInitializer")
class UserRepository(val db: JdbcTemplate) {

    data class User(val id: Int, val username: String, val password: String)

    fun getUserByName(username: String) =
        db.queryForObject(
            "SELECT user_id, username, password FROM user WHERE username = ?",
            arrayOf(username),
            this::userMapper
        )

    private fun userMapper(rs: ResultSet, row: Int) =
        User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password")
        )
}
