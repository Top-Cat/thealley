package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("user", "user_id") {
    val username = varchar("username", 64)
    val password = varchar("password", 255)
}
