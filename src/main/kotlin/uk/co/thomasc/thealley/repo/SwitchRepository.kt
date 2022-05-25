package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.devices.Switch
import uk.co.thomasc.thealley.scenes.Scene

object SwitchTable : IntIdTable("switch") {
    val button = integer("button")
    val scene = reference("scene", SceneTable)
    val state = short("state")
}

object DeviceTable : IntIdTable("device") {
    val hostname = text("hostname")
    val type = customEnumeration("type", null, { value -> SwitchRepository.DeviceType.valueOf((value as String).uppercase()) }, { it.name.lowercase() })
    val name = text("name")
}

class SwitchRepository {
    data class SwitchObj(val key: EntityID<Int>) : IntEntity(key) {
        companion object : IntEntityClass<SwitchObj>(SwitchTable)
        val button by SwitchTable.button
        val scene by SwitchTable.scene
        val state by SwitchTable.state
    }

    data class Device(val key: EntityID<Int>) : IntEntity(key), DeviceMapper.HasDeviceId {
        companion object : IntEntityClass<Device>(DeviceTable)

        override val deviceId by lazy { id.value }
        val hostname by DeviceTable.hostname
        val type by DeviceTable.type
        val name by DeviceTable.name
    }

    enum class DeviceType {
        BULB,
        PLUG,
        RELAY
    }

    fun getDeviceForId(id: Int): Device =
        transaction {
            Device.wrapRow(
                DeviceTable.select {
                    DeviceTable.id eq id
                }.single()
            )
        }

    fun updateSwitchState(switch: Switch) = transaction {
        SwitchTable.update({
            (SwitchTable.id eq switch.obj.id) and (SwitchTable.button eq switch.obj.button)
        }) {
            it[state] = switch.state
        }
    }

    fun getSwitches(scene: Map<Int, Scene>) = transaction {
        SwitchObj.wrapRows(
            SwitchTable.selectAll()
        ).mapNotNull {
            scene[it.scene.value]?.let { so ->
                Switch(this@SwitchRepository, so, it)
            }
        }.associateBy { it.obj.id.value to it.obj.button }
    }

    fun getDevicesForType(type: DeviceType): List<Device> = transaction {
        Device.wrapRows(
            DeviceTable.select {
                DeviceTable.type eq type
            }
        ).toList()
    }

    fun updateSwitch(switchId: Int, buttonId: Int, sceneId: Int) = transaction {
        SwitchTable.insertOrUpdate(SwitchTable.scene) {
            it[id] = switchId
            it[button] = buttonId
            it[scene] = sceneId
            it[state] = 0
        }
    }
}

fun <T : Table> T.insertOrUpdate(vararg onDuplicateUpdateKeys: Column<*>, body: T.(InsertStatement<Number>) -> Unit) =
    InsertOrUpdate<Number>(onDuplicateUpdateKeys, this).apply {
        body(this)
        execute(TransactionManager.current())
    }

class InsertOrUpdate<Key : Any>(
    private val onDuplicateUpdateKeys: Array< out Column<*>>,
    table: Table,
    isIgnore: Boolean = false
) : InsertStatement<Key>(table, isIgnore) {
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDuplicateUpdateKeys.isNotEmpty()) {
            " ON DUPLICATE KEY UPDATE " + onDuplicateUpdateKeys.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        } else ""
        return super.prepareSQL(transaction) + onUpdateSQL
    }
}
