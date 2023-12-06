package uk.co.thomasc.thealley.oauth

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object RefreshTokenTable : IdTable<String>("oa_refresh_token") {
    override val id: Column<EntityID<String>> = varchar("token_id", 256).entityId()
    val scope = varchar("scope", 256)
    val userName = varchar("user_name", 256).nullable()
    val clientId = varchar("client_id", 256)
}
