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
    private lateinit var powerInfo: TexecomPower

    override suspend fun init(bus: AlleyEventBus) {
        bus.handle<MqttMessageEvent> { ev ->
            val parts = ev.topic.split('/')

            if (parts.size < 2 || parts[0] != config.prefix) return@handle
            if (parts[1] == "area" && parts.size > 3) return@handle

            if (parts[1] == "status") {
                // val online = ev.payload == "online"
                return@handle
            }

            messageTypes[parts[1]]?.also {
                handleMessage(ev.payload, parts, it)
            } ?: logger.info { "Texecom received ${ev.topic}: ${ev.payload}" }
        }

        registerGoogleHomeDevice(
            DeviceType.SECURITYSYSTEM,
            true,
            ArmDisarmTrait(
                setOf(
                    ArmLevel("House", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("House")))),
                    ArmLevel("Garage", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("Garage")))),
                    ArmLevel("FULL", setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf("FULL"))))
                ),
                false,
                {
                    val armedAreas = areasInState(true)
                    val armed = armedAreas.isNotEmpty()
                    ArmDisarmTrait.State(
                        armed,
                        if (!armed) null else armedAreas.values.joinToString(",") { it.name },
                        if (!armed) null else 30
                    )
                }
            ) { arm, level ->
                if (arm && level == "FULL") {
                    areasInState(false).forEach { (_, area) ->
                        areaCommand(bus, area.slug!!, AreaCommand.FULL)
                    }
                } else if (arm) {
                    val area = areaState.values.first { it.name == level }
                    areaCommand(bus, area.slug!!, AreaCommand.FULL)
                } else {
                    areasInState(true).forEach { (_, area) ->
                        areaCommand(bus, area.slug!!, AreaCommand.DISARM)
                    }
                }
            }
        )
    }

    private fun areasInState(armed: Boolean) = areaState
        .filter { (_, area) -> (area.status != TexecomAreaStatus.DISARMED) == armed }

    private fun <T : Any> handleMessage(payload: String, parts: List<String>, serializer: KSerializer<T>) {
        when (val msg = alleyJson.decodeFromString(serializer, payload)) {
            is TexecomArea -> areaState[msg.id] = msg.copy(slug = parts[2])
            is TexecomZone -> zoneState[msg.number] = msg.copy(slug = parts[2])
            is TexecomPower -> powerInfo = msg
            else -> logger.info { "Texecom parsed $msg" }
        }
    }

    private suspend fun areaCommand(bus: AlleyEventBus, slug: String, type: AreaCommand) {
        bus.emit(MqttSendEvent("${config.prefix}/area/$slug/command", type.state))
    }

    companion object : KLogging()
}
