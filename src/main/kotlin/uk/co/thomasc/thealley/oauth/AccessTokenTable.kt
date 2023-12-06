package uk.co.thomasc.thealley.oauth

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp

object AccessTokenTable : IdTable<String>("oa_access_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val type = varchar("type", 256)
    val expiration = timestamp("expiration")
    val scope = varchar("scope", 256)
    val userName = varchar("user_name", 256).nullable()
    val clientId = varchar("client_id", 256)
    val refreshToken = varchar("refresh_token", 256).nullable()
}
