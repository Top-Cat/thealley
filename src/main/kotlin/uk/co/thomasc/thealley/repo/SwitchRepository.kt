package uk.co.thomasc.thealley.repo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class SwitchRepository(val db: JdbcTemplate) {

    data class SwitchConfig(
        val macAddr: String,
        val hostA: String,
        val hostB: String
    )

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

    fun getSwitchConfig(macAddr: String): SwitchConfig? =
        db.queryForObject(
            """
                |SELECT switch.macAddr, deviceA.hostname hostA, deviceB.hostname hostB
                |   FROM switch
                |   JOIN device deviceA
                |       ON switch.hostA = deviceA.id
                |   JOIN device deviceB
                |       ON switch.hostB = deviceB.id
                |   WHERE macAddr = ?
            """.trimMargin(),
            arrayOf(macAddr),
            this::switchConfigMapper
        )

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

    private fun switchConfigMapper(rs: ResultSet, row: Int) =
        SwitchConfig(
            rs.getString("macAddr"),
            rs.getString("hostA"),
            rs.getString("hostB")
        )

    private fun deviceMapper(rs: ResultSet, row: Int) =
        Device(
            rs.getInt("id"),
            rs.getString("hostname"),
            rs.getString("name"),
            DeviceType.valueOf(rs.getString("type").toUpperCase())
        )
}
