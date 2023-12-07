package uk.co.thomasc.thealley.devices.onkyo

import mu.KLogging
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

class OnkyoDevice(id: Int, config: OnkyoConfig, state: EmptyState, stateStore: IStateUpdater<EmptyState>) :
    AlleyDevice<OnkyoDevice, OnkyoConfig, EmptyState>(id, config, state, stateStore) {

    private val conn = OnkyoConnection(config.host)

    private suspend fun getPowerState() = conn.send<PowerPacket>(PowerPacket())?.command == PowerPacket.PowerCommand.ON

    private suspend fun setPowerState(state: Boolean) = conn.send<PowerPacket>(
        PowerPacket(if (state) PowerPacket.PowerCommand.ON else PowerPacket.PowerCommand.OFF)
    )

    override suspend fun init(bus: AlleyEventBus) {
        conn.init()
        conn.receive() // Album art is sent immediately?

        registerGoogleHomeDevice(
            DeviceType.AUDIO_VIDEO_RECEIVER,
            InputSelectorTrait(
                orderedInputs = true,
                getInputs = {
                    (conn.send<ReceiverInformationPacket>(ReceiverInformationPacket())?.command as? ReceiverInformationPacket.Command.Data)?.data?.let {
                        it.device.selectorList.list.map { selector ->
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
                    } ?: listOf()
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
                    conn.send<MutingPacket>(MutingPacket())?.command == MutingPacket.MutingCommand.ON
                },
                mute = { mute ->
                    conn.send<MutingPacket>(MutingPacket(if (mute) MutingPacket.MutingCommand.ON else MutingPacket.MutingCommand.OFF))
                },
                getVolume = {
                    // TODO: Throw error if failure?
                    (conn.send<MasterVolumePacket>(MasterVolumePacket())?.command as? MasterVolumePacket.VolumeCommand.Level)?.level ?: 0
                },
                setVolume = { vol ->
                    conn.send<MasterVolumePacket>(MasterVolumePacket(MasterVolumePacket.VolumeCommand.Level(vol)))
                },
                setVolumeRelative = { rel ->
                    val command = when {
                        rel > 2 -> MasterVolumePacket.VolumeCommand.Up
                        rel < -2 -> MasterVolumePacket.VolumeCommand.Down
                        rel > 0 -> MasterVolumePacket.VolumeCommand.Up1
                        else -> MasterVolumePacket.VolumeCommand.Down1
                    }
                    conn.send<MasterVolumePacket>(MasterVolumePacket(command))
                }
            )
        )
    }

    companion object : KLogging()
}
