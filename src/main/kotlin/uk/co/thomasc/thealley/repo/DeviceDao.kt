package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

data class DeviceDao(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<DeviceDao>(DeviceTable)

    val config by DeviceTable.config
    val state by DeviceTable.state

    val createdAt by DeviceTable.createdAt
    val updatedAt by DeviceTable.updatedAt
    val configUpdatedAt by DeviceTable.configUpdatedAt
}
