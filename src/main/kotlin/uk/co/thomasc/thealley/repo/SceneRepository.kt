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
import java.time.LocalDateTime

@Component
@DependsOn("flywayInitializer")
class SceneRepository(
    val db: JdbcTemplate,
    private val localClient: LocalClient,
    private val relayClient: RelayClient,
    private val switchRepository: SwitchRepository) {

    fun getRules(): List<Rule> =
        db.query(
            "SELECT rule_id, timeout, last_active, last_updated, daytime, off_at, scene_id FROM rule",
            this::rulesMapper
        )

    fun getSensors(ruleId: Int): List<String> =
        db.query(
            "SELECT sensor_id FROM rule_sensor WHERE rule_id = ?",
            arrayOf(ruleId),
            this::sensorMapper
        )

    fun getScene(sceneId: Int) =
        Scene(
            localClient,
            relayClient,
            switchRepository,
            db.query(
                "SELECT light_id, brightness, hue FROM scene WHERE scene_id = ?",
                arrayOf(sceneId),
                this::sceneMapper
            )
        )

    private fun sceneMapper(rs: ResultSet, row: Int) =
        ScenePart(
            rs.getInt("light_id"),
            rs.getInt("brightness"),
            rs.getInt("hue")
        )

    private fun sensorMapper(rs: ResultSet, row: Int) =
        rs.getString("sensor_id")

    private fun rulesMapper(rs: ResultSet, row: Int) =
        Rule(
            this,
            rs.getInt("rule_id"),
            getSensors(rs.getInt("rule_id")),
            rs.getInt("timeout"),
            rs.getTimestamp("last_active").toLocalDateTime(),
            rs.getTimestamp("off_at")?.toLocalDateTime(),
            rs.getTimestamp("last_updated").toLocalDateTime(),
            rs.getBoolean("daytime"),
            getScene(rs.getInt("scene_id"))
        )

    fun updateLastActive(obj: Rule) {
        db.update(
            "UPDATE rule SET last_active = ?, last_updated = ?, off_at = ? WHERE rule_id = ?",
            obj.lastActive, obj.lastUpdated, obj.offAt, obj.id
        )
    }
}
