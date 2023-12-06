package uk.co.thomasc.thealley.repo

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import uk.co.thomasc.thealley.devices.types.IAlleyConfig

object DeviceTable : IntIdTable("devicev2") {
    val config = json<IAlleyConfig>("config", json = Json)
    val state = text("state")
    val createdAt = timestamp("createdAt")
    val configUpdatedAt = timestamp("configUpdatedAt")
    val updatedAt = timestamp("updatedAt")
}
