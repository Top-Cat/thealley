package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

data class User(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<User>(UserTable)
    val username by UserTable.username
    val password by UserTable.password
}
