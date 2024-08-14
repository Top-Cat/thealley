package uk.co.thomasc.thealley.repo

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import uk.co.thomasc.thealley.devices.types.IAlleyConfigBase

object DeviceTable : IntIdTable("devicev2") {
    val config = json<IAlleyConfigBase>("config", json = Json)
    val state = text("state")
    val enabled = bool("enabled")
    val createdAt = timestamp("createdAt")
    val configUpdatedAt = timestamp("configUpdatedAt")
    val updatedAt = timestamp("updatedAt")
}
