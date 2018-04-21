package uk.co.thomasc.thealley.repo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.scenes.Rule
import uk.co.thomasc.thealley.scenes.Scene
import uk.co.thomasc.thealley.scenes.ScenePart
import java.sql.ResultSet
import java.time.LocalDateTime

@Component
class SceneRepository(val db: JdbcTemplate, private val kasa: LocalClient) {

    fun getRules(): List<Rule> =
        db.query(
            "SELECT rule_id, sensor_id, state, last_active, scene_id FROM rule",
            this::rulesMapper
        )

    fun getScene(sceneId: Int) =
        Scene(
            kasa,
            db.query(
                "SELECT light_id, brightness, hue FROM scene WHERE scene_id = ?",
                arrayOf(sceneId),
                this::sceneMapper
            )
        )

    private fun sceneMapper(rs: ResultSet, row: Int) =
        ScenePart(
            rs.getString("light_id"),
            rs.getInt("brightness"),
            rs.getInt("hue")
        )

    private fun rulesMapper(rs: ResultSet, row: Int) =
        Rule(
            this,
            rs.getInt("rule_id"),
            rs.getString("sensor_id"),
            rs.getBoolean("state"),
            rs.getTimestamp("last_active").toLocalDateTime(),
            getScene(rs.getInt("scene_id"))
        )

    fun updateLastActive(id: Int, lastActive: LocalDateTime?) {
        db.update("UPDATE rule SET last_active = ? WHERE id = ?", arrayOf(lastActive, id))
    }
}
