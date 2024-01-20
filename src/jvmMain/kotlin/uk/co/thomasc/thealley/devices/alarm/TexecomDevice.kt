package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.KSerializer
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.ReportStateEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttMessageEvent
import uk.co.thomasc.thealley.devices.system.mqtt.MqttSendEvent
import uk.co.thomasc.thealley.devices.types.TexecomConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.GoogleHomeLang
import uk.co.thomasc.thealley.google.trait.ArmDisarmTrait
import uk.co.thomasc.thealley.google.trait.ArmLevel
import uk.co.thomasc.thealley.web.google.AlleyDeviceInfo

class TexecomDevice(id: Int, config: TexecomConfig, state: TexecomState, stateStore: IStateUpdater<TexecomState>) :
    AlleyDevice<TexecomDevice, TexecomConfig, TexecomState>(id, config, state, stateStore) {

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
    private lateinit var deviceInfo: TexecomInfo

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
                handleMessage(bus, ev.payload, parts, it)
            } ?: logger.info { "Texecom received ${ev.topic}: ${ev.payload}" }
        }

        registerGoogleHomeDevice(
            DeviceType.SECURITYSYSTEM,
            true,
            {
                AlleyDeviceInfo("Texecom", deviceInfo.model, deviceInfo.firmwareVersion, deviceInfo.version)
            },
            ArmDisarmTrait(
                GoogleArmLevel.entries.mapNotNull { it.armLevel }.map {
                    ArmLevel(it, setOf(ArmLevel.Value(GoogleHomeLang.ENGLISH, listOf(it))))
                }.toSet(),
                false,
                {
                    val level = getLevel()
                    val armed = level != GoogleArmLevel.NONE

                    ArmDisarmTrait.State(
                        armed,
                        getLevel().armLevel,
                        if (!armed) null else 30
                    )
                }
            ) { arm, level ->
                if (arm && (level == null || level == "FULL")) {
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

    private suspend fun <T : Any> handleMessage(bus: AlleyEventBus, payload: String, parts: List<String>, serializer: KSerializer<T>) {
        when (val msg = alleyJson.decodeFromString(serializer, payload)) {
            is TexecomArea -> {
                areaState[msg.id] = msg.copy(slug = parts[2])
                checkState(bus)
            }
            is TexecomZone -> zoneState[msg.number] = msg.copy(slug = parts[2])
            is TexecomPower -> powerInfo = msg
            is TexecomInfo -> deviceInfo = msg
            else -> logger.info { "Texecom parsed $msg" }
        }
    }

    private fun getLevel(): GoogleArmLevel {
        val currentState = areasInState(true).values.map { it.name }.sorted().joinToString(",")
        return GoogleArmLevel.entries.find { it.str == currentState } ?: GoogleArmLevel.FULL
    }

    private suspend fun checkState(bus: AlleyEventBus) {
        if (updateState(state.copy(armLevel = getLevel()))) {
            bus.emit(ReportStateEvent(this))
        }
    }

    private suspend fun areaCommand(bus: AlleyEventBus, slug: String, type: AreaCommand) {
        bus.emit(MqttSendEvent("${config.prefix}/area/$slug/command", type.state))
    }

    companion object : KLogging()
}
