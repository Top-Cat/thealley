package uk.co.thomasc.thealley.devices.onkyo.packet

import uk.co.thomasc.thealley.devices.onkyo.UnknownPacketException

data class UpdatePacket(val command: Command) : IOnkyoResponse("UPD") {
    constructor(command: String) : this(
        when {
            command == QUERY -> Command.Query
            command == "CMP" -> Command.UpdateComplete
            command[0] == 'D' -> Command.DownloadStatus(Command.DownloadStatus.Status.fromString(command.substring(1)))
            command[0] == '0' -> Command.FirmwareStatus(Command.FirmwareStatus.Status.fromString(command))
            else -> throw UnknownPacketException(command)
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data object Query : Command(QUERY)
        data object UpdateComplete : Command("CMP")
        data class DownloadStatus(val status: Status) : Command("D${status.encoding}") {
            enum class Status(val encoding: String) {
                DOWNLOADING("DL"),
                ARMWRITE("NT"),
                DSP1("D1"),
                DSP2("D2"),
                DSP3("D3"),
                VMPU("VM"),
                OSD("OS"),
                MP("MMPU"),
                NONE("");

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromString(c: String) = lookup[c] ?: NONE
                }
            }
        }
        data class FirmwareStatus(val status: Status) : Command(status.encoding) {
            enum class Status(val encoding: String) {
                NONE("00"), NEW("01"), FORCE("02");

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromString(c: String) = lookup[c] ?: NONE
                }
            }
        }
    }
}
