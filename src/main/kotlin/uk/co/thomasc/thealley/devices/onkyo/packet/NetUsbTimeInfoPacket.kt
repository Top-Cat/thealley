package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbTimeInfoPacket(val command: Command) : IOnkyoResponse("NTM") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> {
                val parts = command.split("/")
                Command.Elapsed(TimeCodeUtils.toInt(parts[0]), TimeCodeUtils.toInt(parts[1]))
            }
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Elapsed(val elapsed: Int?, val total: Int?) : Command("${TimeCodeUtils.fromInt(elapsed, 3)}/${TimeCodeUtils.fromInt(total, 3)}")
    }
}
