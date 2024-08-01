package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
    fun getUserByName(username: String) = transaction {
        UserTable.selectAll().where {
            UserTable.username eq username
        }.singleOrNull()?.let { User.wrapRow(it) }
    }
}
