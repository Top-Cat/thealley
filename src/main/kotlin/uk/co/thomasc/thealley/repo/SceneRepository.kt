package uk.co.thomasc.thealley.repo

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import uk.co.thomasc.thealley.devices.DeviceMapper
import uk.co.thomasc.thealley.scenes.Rule
import uk.co.thomasc.thealley.scenes.RuleObj
import uk.co.thomasc.thealley.scenes.Scene

object SceneTable : IntIdTable("scene") {
    val sceneId = integer("scene_id")
    val lightId = integer("light_id")
    val brightness = integer("brightness")
    val hue = integer("hue")
    val saturation = integer("saturation")
    val colorTemp = integer("color_temp")
}

object RuleTable : IntIdTable("rule", "rule_id") {
    val lastActive = datetime("last_active")
    val scene = reference("scene_id", SceneTable)
    val timeout = integer("timeout")
    val lastUpdated = datetime("last_updated")
    val daytime = bool("daytime")
    val offAt = datetime("off_at").nullable()
}

object RuleSensorTable : Table("rule_sensor") {
    val rule = reference("rule_id", RuleTable)
    val sensor = varchar("sensor_id", 32)
}

class SceneRepository(private val deviceMapper: DeviceMapper) {
    data class ScenePart(val key: EntityID<Int>) : IntEntity(key), DeviceMapper.HasDeviceId {
        companion object : IntEntityClass<ScenePart>(SceneTable)
        val sceneId by SceneTable.sceneId
        val lightId by SceneTable.lightId
        override val deviceId by lazy { lightId }

        val brightness by SceneTable.brightness
        val hue by SceneTable.hue
        val saturation by SceneTable.saturation
        val colorTemp by SceneTable.colorTemp
    }

    fun getScenes() = transaction {
        ScenePart.wrapRows(SceneTable.selectAll())
            .groupBy { it.sceneId }
            .map {
                it.key to Scene(
                    it.key,
                    deviceMapper,
                    it.value
                )
            }
            .toMap()
    }

    fun getRules(scene: Map<Int, Scene>) = transaction {
        RuleObj.wrapRows(
            RuleTable.selectAll()
        ).mapNotNull { ro ->
            scene[ro.scene.value]?.let {
                Rule(
                    this@SceneRepository,
                    getSensors(ro.id.value),
                    it,
                    ro
                )
            }
        }
    }

    fun getSensors(ruleId: Int) = transaction {
        RuleSensorTable.select {
            RuleSensorTable.rule eq ruleId
        }.map {
            it[RuleSensorTable.sensor]
        }
    }

    fun updateLastActive(id: Int, obj: Rule) = transaction {
        RuleTable.update({
            RuleTable.id eq id
        }) {
            it[lastActive] = obj.lastActive
            it[lastUpdated] = obj.lastUpdated
            it[offAt] = obj.offAt
        }
    }
}
