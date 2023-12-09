package uk.co.thomasc.thealley.devices.onkyo

import mu.KLogging
import uk.co.thomasc.thealley.cached
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.packet.InputPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MasterVolumePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MutingPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.PowerPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.ReceiverInformationPacket
import uk.co.thomasc.thealley.devices.types.OnkyoConfig
import uk.co.thomasc.thealley.google.DeviceType
import uk.co.thomasc.thealley.google.GoogleHomeLang
import uk.co.thomasc.thealley.google.trait.InputSelectorInput
import uk.co.thomasc.thealley.google.trait.InputSelectorTrait
import uk.co.thomasc.thealley.google.trait.MediaStateTrait
import uk.co.thomasc.thealley.google.trait.OnOffTrait
import uk.co.thomasc.thealley.google.trait.TransportControlTrait
import uk.co.thomasc.thealley.google.trait.VolumeTrait
import kotlin.time.Duration.Companion.hours

class OnkyoDevice(id: Int, config: OnkyoConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<OnkyoDevice, OnkyoConfig, EmptyState>(id, config, state, stateStore) {

    private val conn = OnkyoConnection(config.host)

    private suspend fun getPowerState() = conn.send<PowerPacket>(PowerPacket())?.command == PowerPacket.PowerCommand.On

    private suspend fun setPowerState(state: Boolean) = conn.send<PowerPacket>(
        PowerPacket(if (state) PowerPacket.PowerCommand.On else PowerPacket.PowerCommand.Off)
    )

    private val information by cached(1.hours) {
        val cmd = conn.send<ReceiverInformationPacket>(ReceiverInformationPacket())?.command
        if (cmd is ReceiverInformationPacket.Command.Data) cmd.data else null
    }

    override suspend fun init(bus: AlleyEventBus) {
        conn.init()

        registerGoogleHomeDevice(
            DeviceType.AUDIO_VIDEO_RECEIVER,
            InputSelectorTrait(
                orderedInputs = true,
                getInputs = {
                    information.device.selectorList.list.filter { selector ->
                        selector.value && selector.id != "80"
                    }.map { selector ->
                        InputSelectorInput(
                            selector.id,
                            listOf(
                                InputSelectorInput.LocalizedName(
                                    GoogleHomeLang.ENGLISH,
                                    listOf(
                                        selector.name
                                    )
                                )
                            )
                        )
                    }
                },
                getCurrentInput = {
                    // TODO: Handle failure
                    (conn.send<InputPacket>(InputPacket())?.command as? InputPacket.InputCommand.Input)?.id ?: ""
                },
                nextInput = {
                    conn.send<InputPacket>(InputPacket(InputPacket.InputCommand.Down))
                },
                previousInput = {
                    conn.send<InputPacket>(InputPacket(InputPacket.InputCommand.Up))
                },
                setInput = { inputName ->
                    conn.send<InputPacket>(InputPacket(InputPacket.InputCommand.Input(inputName)))
                }
            ),
            MediaStateTrait(
                // TODO: This trait
            ),
            OnOffTrait(
                getOnOff = ::getPowerState,
                setOnOff = ::setPowerState
            ),
            TransportControlTrait(
                // TODO: This trait
            ),
            VolumeTrait(
                volumeMaxLevel = 80,
                volumeCanMuteAndUnmute = true,
                levelStepSize = 5,
                isMuted = {
                    conn.send<MutingPacket>(MutingPacket())?.command == MutingPacket.MutingCommand.On
                },
                mute = { mute ->
                    val res = conn.send<MutingPacket>(MutingPacket(if (mute) MutingPacket.MutingCommand.On else MutingPacket.MutingCommand.Off))
                    res?.command == MutingPacket.MutingCommand.On
                },
                getVolume = {
                    // TODO: Throw error if failure?
                    (conn.send<MasterVolumePacket>(MasterVolumePacket())?.command as? MasterVolumePacket.VolumeCommand.Level)?.level ?: 0
                },
                setVolume = { vol ->
                    val res = conn.send<MasterVolumePacket>(MasterVolumePacket(MasterVolumePacket.VolumeCommand.Level(vol)))
                    (res?.command as? MasterVolumePacket.VolumeCommand.Level)?.level
                },
                setVolumeRelative = { rel ->
                    val command = when {
                        rel > 2 -> MasterVolumePacket.VolumeCommand.Up
                        rel < -2 -> MasterVolumePacket.VolumeCommand.Down
                        rel > 0 -> MasterVolumePacket.VolumeCommand.Up1
                        else -> MasterVolumePacket.VolumeCommand.Down1
                    }
                    val res = conn.send<MasterVolumePacket>(MasterVolumePacket(command))
                    (res?.command as? MasterVolumePacket.VolumeCommand.Level)?.level
                }
            )
        )
    }

    companion object : KLogging()
}
