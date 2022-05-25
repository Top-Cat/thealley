package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object UserTable : IntIdTable("user", "user_id") {
    val username = varchar("username", 64)
    val password = varchar("password", 255)
}

data class User(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<User>(UserTable)
    val username by UserTable.username
    val password by UserTable.password
}

class UserRepository {
    fun getUserByName(username: String) = transaction {
        UserTable.select {
            UserTable.username eq username
        }.singleOrNull()?.let { User.wrapRow(it) }
    }
}
