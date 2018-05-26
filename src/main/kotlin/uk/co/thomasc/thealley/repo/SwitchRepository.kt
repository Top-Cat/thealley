package uk.co.thomasc.thealley.repo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.devices.Switch
import uk.co.thomasc.thealley.scenes.Scene
import java.sql.ResultSet

@Component
class SwitchRepository(val db: JdbcTemplate) {

    data class Device(
        val id: Int,
        val hostname: String,
        val name: String,
        val type: DeviceType
    )

    enum class DeviceType {
        BULB,
        PLUG,
        RELAY
    }

    fun getSwitches(scene: Map<Int, Scene>): Map<Pair<Int, Int>, Switch> {

        fun switchConfigMapper(rs: ResultSet, row: Int) =
            scene[rs.getInt("scene")]?.let {
                Switch(
                    rs.getInt("id"),
                    rs.getInt("button"),
                    rs.getInt("state"),
                    it,
                    this
                )
            }

        return db.query(
            "SELECT switch.id, switch.button, switch.scene, switch.state FROM switch",
            ::switchConfigMapper
        ).filterNotNull().associateBy { it.switchId to it.buttonId }
    }

    fun updateSwitchState(switch: Switch) {
        db.update(
            "UPDATE switch SET state = ? WHERE button = ? AND id = ?",
            switch.state,
            switch.buttonId,
            switch.switchId
        )
    }

    fun getDevicesForType(type: DeviceType): List<Device> =
        db.query(
            """
                |SELECT id, hostname, name, type
                |   FROM device
                |   WHERE type = ?
            """.trimMargin(),
            arrayOf(type.toString()),
            this::deviceMapper
        )

    fun getDeviceForId(id: Int): Device =
        db.queryForObject(
            """
                |SELECT id, hostname, name, type
                |   FROM device
                |   WHERE id = ?
            """.trimMargin(),
            arrayOf(id),
            this::deviceMapper
        )!!

    private fun deviceMapper(rs: ResultSet, row: Int) =
        Device(
            rs.getInt("id"),
            rs.getString("hostname"),
            rs.getString("name"),
            DeviceType.valueOf(rs.getString("type").toUpperCase())
        )
}
