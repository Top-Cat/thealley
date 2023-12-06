package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
    fun getUserByName(username: String) = transaction {
        UserTable.select {
            UserTable.username eq username
        }.singleOrNull()?.let { User.wrapRow(it) }
    }
}
