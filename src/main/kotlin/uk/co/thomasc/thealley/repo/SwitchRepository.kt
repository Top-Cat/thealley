package uk.co.thomasc.thealley.repo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.scenes.Scene
import java.sql.ResultSet
import kotlin.math.max
import kotlin.math.min

@Component
class SwitchRepository(val db: JdbcTemplate) {

    data class Switch(
        val switchId: Int,
        val buttonId: Int,
        var state: Int,
        private val scene: Scene,
        private val switchRepository: SwitchRepository
    ) {
        private var fadeStarted: Long = 0

        fun toggle() {
            if (state > 0) {
                state = 0
                scene.off()
            } else {
                state = 100
                scene.execute()
            }

            switchRepository.updateSwitchState(this)
        }

        fun startFade() {
            fadeStarted = System.currentTimeMillis()
            if (state > 0) {
                scene.execute(0, state * 100)
            } else {
                scene.execute(100, 10000)
            }
        }

        fun endFade() {
            val fadeTime = System.currentTimeMillis() - fadeStarted

            state = if (state > 0) {
                max(((state * 100) - fadeTime) / 100, 1)
            } else {
                min(fadeTime / 100, 100)
            }.toInt()

            scene.execute(state, 0)
            switchRepository.updateSwitchState(this)
        }
    }

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
