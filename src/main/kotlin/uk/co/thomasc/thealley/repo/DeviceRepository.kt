package uk.co.thomasc.thealley.repo

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import uk.co.thomasc.thealley.devicev2.IAlleyConfig

object NewDeviceTable : IntIdTable("devicev2") {
    val config = json<IAlleyConfig>("config", json = Json)
    val state = text("state")
    val createdAt = timestamp("createdAt")
    val configUpdatedAt = timestamp("configUpdatedAt")
    val updatedAt = timestamp("updatedAt")
}

data class NewDeviceDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<NewDeviceDao>(NewDeviceTable)

    val config by NewDeviceTable.config
    val state by NewDeviceTable.state

    val createdAt by NewDeviceTable.createdAt
    val updatedAt by NewDeviceTable.updatedAt
    val configUpdatedAt by NewDeviceTable.configUpdatedAt
}
