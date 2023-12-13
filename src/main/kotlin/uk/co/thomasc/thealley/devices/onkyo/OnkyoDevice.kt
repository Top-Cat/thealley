package uk.co.thomasc.thealley.devices.onkyo

import mu.KLogging
import uk.co.thomasc.thealley.cached
import uk.co.thomasc.thealley.devices.AlleyDevice
import uk.co.thomasc.thealley.devices.AlleyEventBus
import uk.co.thomasc.thealley.devices.EmptyState
import uk.co.thomasc.thealley.devices.GetStateException
import uk.co.thomasc.thealley.devices.IStateUpdater
import uk.co.thomasc.thealley.devices.onkyo.packet.InputPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MasterVolumePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MutingPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbControlPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbPlayStatusPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbTimeSeekPacket
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
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode
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

    private var playStatus: NetUsbPlayStatusPacket.Command.Info.PlayStatus? = null
    private var repeatStatus: NetUsbPlayStatusPacket.Command.Info.RepeatStatus? = null

    override suspend fun init(bus: AlleyEventBus) {
        conn.init()
        conn.handle<NetUsbPlayStatusPacket> {
            if (it.command is NetUsbPlayStatusPacket.Command.Info) {
                if (it.command.playStatus != NetUsbPlayStatusPacket.Command.Info.PlayStatus.EOF) {
                    playStatus = it.command.playStatus
                }
                if (it.command.repeatStatus != NetUsbPlayStatusPacket.Command.Info.RepeatStatus.DISABLE) {
                    repeatStatus = it.command.repeatStatus
                }
            }
        }

        registerGoogleHomeDevice(
            DeviceType.AUDIO_VIDEO_RECEIVER,
            false,
            InputSelectorTrait(
                orderedInputs = true,
                getInputs = {
                    information.device.selectorList.list.filter { selector ->
                        selector.isEnabled() && selector.id != "80"
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
                    (conn.send<InputPacket>(InputPacket())?.command as? InputPacket.InputCommand.Input)?.id
                        ?: throw GetStateException(GoogleHomeErrorCode.TransientError)
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
                transportControlSupportedCommands = listOf(
                    TransportControlTrait.ControlCommand.PAUSE,
                    TransportControlTrait.ControlCommand.RESUME,
                    TransportControlTrait.ControlCommand.NEXT,
                    TransportControlTrait.ControlCommand.PREVIOUS,
                    TransportControlTrait.ControlCommand.STOP,
                    TransportControlTrait.ControlCommand.SEEK_TO_POSITION,
                    TransportControlTrait.ControlCommand.SET_REPEAT,
                    TransportControlTrait.ControlCommand.SHUFFLE
                ),
                pause = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.Pause))
                },
                resume = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.Play))
                },
                next = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.TrackUp))
                },
                previous = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.TrackDown))
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.TrackDown))
                },
                stop = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.Stop))
                },
                seek = { timeMs ->
                    conn.sendOnly(NetUsbTimeSeekPacket(NetUsbTimeSeekPacket.Command.Time(timeMs / 1000)))
                },
                repeat = { isOn, _ ->
                    val currentlyRepeating = repeatStatus == NetUsbPlayStatusPacket.Command.Info.RepeatStatus.OFF
                    if (isOn != currentlyRepeating) conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.Repeat))
                },
                shuffle = {
                    conn.sendOnly(NetUsbControlPacket(NetUsbControlPacket.Command.Random))
                }
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
                    (conn.send<MasterVolumePacket>(MasterVolumePacket())?.command as? MasterVolumePacket.VolumeCommand.Level)?.level
                        ?: throw GetStateException(GoogleHomeErrorCode.TransientError)
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
