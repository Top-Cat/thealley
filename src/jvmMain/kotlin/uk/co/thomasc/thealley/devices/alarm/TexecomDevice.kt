package uk.co.thomasc.thealley.devices.alarm

import kotlinx.serialization.KSerializer
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.alarm.events.TexecomAreaEvent
import uk.co.thomasc.thealley.devices.alarm.events.TexecomZoneEvent
import uk.co.thomasc.thealley.devices.system.ReportStateEvent
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

    private lateinit var powerInfo: TexecomPower
    private lateinit var deviceInfo: TexecomInfo

    private lateinit var armState: ArmDisarmTrait.State

    override suspend fun init(bus: AlleyEventBus) {
        armState = stateFor(state.armLevel)

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
                true,
                {
                    armState
                }
            ) { arm, level ->
                val newLevel = if (!arm) {
                    GoogleArmLevel.NONE
                } else {
                    GoogleArmLevel.entries.find { it.armLevel == level } ?: GoogleArmLevel.FULL
                }
                armState = stateFor(newLevel)

                if (arm) {
                    newLevel.areas
                        .mapNotNull { state.areaState[it] }
                        .filter { it.status == TexecomAreaStatus.DISARMED }
                        .mapNotNull { it.slug }
                        .forEach { slug ->
                            areaCommand(bus, slug, AreaCommand.FULL)
                        }
                } else {
                    areasInState(true).values.mapNotNull { it.slug }.forEach { slug ->
                        areaCommand(bus, slug, AreaCommand.DISARM)
                    }
                }
            }
        )
    }

    private fun stateFor(level: GoogleArmLevel) =
        (level != GoogleArmLevel.NONE).let { armed ->
            ArmDisarmTrait.State(
                armed,
                level.armLevel,
                if (!armed) null else 30
            )
        }

    private fun areasInState(armed: Boolean) = state.areaState
        .filter { (_, area) -> (area.status != TexecomAreaStatus.DISARMED) == armed }

    private suspend fun <T : Any> handleMessage(bus: AlleyEventBus, payload: String, parts: List<String>, serializer: KSerializer<T>) {
        when (val msg = alleyJson.decodeFromString(serializer, payload)) {
            is TexecomArea -> {
                val didChange = updateState {
                    state.copy(
                        areaState = state.areaState.plus(
                            msg.id to msg.copy(slug = parts[2])
                        )
                    )
                }

                if (didChange) {
                    bus.emit(TexecomAreaEvent(msg.number, msg.status))
                }
                checkState(bus)
            }
            is TexecomZone -> {
                val didChange = updateState {
                    state.copy(
                        zoneState = state.zoneState.plus(
                            msg.number to msg.copy(slug = parts[2])
                        )
                    )
                }

                if (didChange) {
                    bus.emit(TexecomZoneEvent(msg.number, msg.status))
                }
            }
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
            armState = stateFor(state.armLevel)
            bus.emit(ReportStateEvent(this))
        }
    }

    private suspend fun areaCommand(bus: AlleyEventBus, slug: String, type: AreaCommand) {
        bus.emit(MqttSendEvent("${config.prefix}/area/$slug/command", type.state))
    }

    companion object : KLogging()
}
