package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.KSerializer
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.TexecomConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.GoogleHomeLang
import uk.co.thomasc.thealley.google.trait.ArmDisarmTrait
import uk.co.thomasc.thealley.google.trait.ArmLevel

class TexecomDevice(id: Int, config: TexecomConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<TexecomDevice, TexecomConfig, EmptyState>(id, config, state, stateStore) {

    private val messageTypes = mapOf(
        "zone" to TexecomZone.serializer(),
        "area" to TexecomArea.serializer(),
        "power" to TexecomPower.serializer(),
        "log" to TexecomEvent.serializer(),
        "config" to TexecomInfo.serializer()
    )

    private val areaState = mutableMapOf<String, TexecomArea>()
    private val zoneState = mutableMapOf<Int, TexecomZone>()

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 2 || parts[0] != config.prefix) return@handle

            if (parts[1] == "status") {
                val online = ev.payload == "online"
                logger.info { "Texecom online: $online" }
                return@handle
            }

            messageTypes[parts[1]]?.also {
                handleMessage(ev.payload, parts, it)
            } ?: logger.info { "Texecom received ${ev.topic}: ${ev.payload}" }
        }

        registerGoogleHomeDevice(
            DeviceType.SECURITYSYSTEM,
            false,
            ArmDisarmTrait(
                setOf(
                    ArmLevel("House", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("House")))),
                    ArmLevel("Garage", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("Garage")))),
                    ArmLevel("FULL", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("FULL"))))
                ),
                false,
                {
                    val armedAreas = areasInState(true)
                    ArmDisarmTrait.State(
                        armedAreas.isNotEmpty(),
                        armedAreas.values.joinToString(","),
                        30
                    )
                }
            ) { arm, level ->
                if (arm && level == "FULL") {
                    areasInState(false).forEach { (_, area) ->
                        areaCommand(bus, area.slug ?: "", AreaCommand.FULL)
                    }
                } else if (arm) {
                    areaCommand(bus, level, AreaCommand.FULL)
                } else {
                    areasInState(true).forEach { (_, area) ->
                        areaCommand(bus, area.slug ?: "", AreaCommand.DISARM)
                    }
                }
            }
        )
    }

    private fun areasInState(armed: Boolean) = areaState
        .filter { (_, area) -> (area.status != TexecomAreaStatus.DISARMED) == armed }

    private fun <T : Any> handleMessage(payload: String, parts: List<String>, serializer: KSerializer<T>) {
        val msg = alleyJson.decodeFromString(serializer, payload)
        logger.info { "Texecom parsed $msg" }

        when (msg) {
            is TexecomArea -> areaState[msg.id] = msg.copy(slug = parts[2])
            is TexecomZone -> zoneState[msg.number] = msg.copy(slug = parts[2])
        }
    }

    private suspend fun areaCommand(bus: AlleyEventBus, slug: String, type: AreaCommand) {
        bus.emit(MqttSendEvent("${config.prefix}/area/$slug/command", type.state))
    }

    companion object : KLogging()
}
