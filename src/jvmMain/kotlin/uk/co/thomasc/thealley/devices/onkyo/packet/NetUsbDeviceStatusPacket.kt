package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbDeviceStatusPacket(val command: Command) : IOnkyoResponse("NDS") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> Command.Query
            else -> Command.Info(
                Command.Info.NetStatus.fromChar(command[0]),
                Command.Info.UsbStatus.fromChar(command[1]),
                Command.Info.UsbStatus.fromChar(command[2])
            )
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data class Info(
            val netStatus: NetStatus,
            val frontUsbStatus: UsbStatus,
            val rearUsbStatus: UsbStatus
        ) : Command("${netStatus.encoding}${frontUsbStatus.encoding}${rearUsbStatus.encoding}") {
            enum class NetStatus(val encoding: Char) {
                NoConnection('-'), Ethernet('E'), Wireless('W');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: NoConnection
                }
            }

            enum class UsbStatus(val encoding: Char) {
                NoDevice('-'), Apple('i'), Drive('M'), Wireless('W'), Bluetooth('B'), DISABLE('x');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: DISABLE
                }
            }
        }
    }
}
