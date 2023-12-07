package uk.co.thomasc.thealley.devices.onkyo.packet

data class ArtPacket(val command: ArtCommand = ArtCommand.Status) : IOnkyoResponse("NJA") {
    constructor(command: String) : this(
        when (command) {
            QUERY -> ArtCommand.Status
            else -> ArtCommand.Data(
                ArtType.fromChar(command[0]),
                ArtFlag.fromChar(command[1]),
                command.substring(2)
            )
        }
    )

    override fun toMessage() = command.cmd

    sealed class ArtCommand(val cmd: String) {
        data object Status : ArtCommand(QUERY)
        data object Request : ArtCommand("REQ")
        data object Up : ArtCommand("UP")
        data object Link : ArtCommand("LINK")
        data object BMP : ArtCommand("BMP")
        data object Enable : ArtCommand("ENA")
        data object Disable : ArtCommand("DIS")
        data class Data(val type: ArtType, val flag: ArtFlag, val data: String) : ArtCommand("${type.c}${flag.c}$data")
    }

    enum class ArtType(val c: Char) {
        BMP('0'), JPEG('1'), URL('2'), NONE('n');

        companion object {
            private val lookup = entries.associateBy { it.c }
            fun fromChar(c: Char) = lookup[c] ?: NONE
        }
    }

    enum class ArtFlag(val c: Char) {
        Start('0'), Next('1'), End('2'), UNUSED('-');

        companion object {
            private val lookup = entries.associateBy { it.c }
            fun fromChar(c: Char) = lookup[c] ?: UNUSED
        }
    }
}
