package uk.co.thomasc.thealley.repo

import org.springframework.context.annotation.DependsOn
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.client.RelayClient
import uk.co.thomasc.thealley.scenes.Rule
import uk.co.thomasc.thealley.scenes.Scene
import uk.co.thomasc.thealley.scenes.ScenePart
import java.sql.ResultSet

@Component
@DependsOn("flywayInitializer")
class SceneRepository(
    val db: JdbcTemplate,
    private val localClient: LocalClient,
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    fun getRules(scene: Map<Int, Scene>): List<Rule> {

        fun rulesMapper(rs: ResultSet, row: Int) =
            scene[rs.getInt("scene_id")]?.let {
                Rule(
                    this,
                    rs.getInt("rule_id"),
                    getSensors(rs.getInt("rule_id")),
                    rs.getInt("timeout"),
                    rs.getTimestamp("last_active").toLocalDateTime(),
                    rs.getTimestamp("off_at")?.toLocalDateTime(),
                    rs.getTimestamp("last_updated").toLocalDateTime(),
                    rs.getBoolean("daytime"),
                    it
                )
            }

        return db.query(
            "SELECT rule_id, timeout, last_active, last_updated, daytime, off_at, scene_id FROM rule",
            ::rulesMapper
        ).filterNotNull()
    }

    fun getSensors(ruleId: Int): List<String> =
        db.query(
            "SELECT sensor_id FROM rule_sensor WHERE rule_id = ?",
            arrayOf(ruleId),
            this::sensorMapper
        )

    fun getScenes() =
        db.query(
            "SELECT scene_id, light_id, brightness, hue, color_temp, saturation FROM scene",
            this::sceneMapper
        )
            .groupBy { it.sceneId }
            .map {
                it.key to Scene(
                    it.key,
                    localClient,
                    relayClient,
                    switchRepository,
                    it.value
                )
            }
            .toMap()

    private fun sceneMapper(rs: ResultSet, row: Int) =
        ScenePart(
            rs.getInt("scene_id"),
            rs.getInt("light_id"),
            rs.getInt("brightness"),
            rs.getInt("hue").let { if (rs.wasNull()) null else it },
            rs.getInt("saturation").let { if (rs.wasNull()) null else it },
            rs.getInt("color_temp").let { if (rs.wasNull()) null else it }
        )

    private fun sensorMapper(rs: ResultSet, row: Int) =
        rs.getString("sensor_id")

    fun updateLastActive(obj: Rule) {
        db.update(
            "UPDATE rule SET last_active = ?, last_updated = ?, off_at = ? WHERE rule_id = ?",
            obj.lastActive, obj.lastUpdated, obj.offAt, obj.id
        )
    }
}
