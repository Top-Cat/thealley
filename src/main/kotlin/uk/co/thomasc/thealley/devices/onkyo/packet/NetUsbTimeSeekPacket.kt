package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbTimeSeekPacket(val command: Command) : IOnkyoResponse("NTS") {
    constructor(command: String) : this(
        Command.Time(TimeCodeUtils.toInt(command))
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data class Time(val total: Int?) : Command(TimeCodeUtils.fromInt(total, 3))
    }
}
