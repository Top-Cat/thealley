package uk.co.thomasc.thealley.devices.onkyo.packet

data class NetUsbListInfoPacket(val command: Command) : IOnkyoResponse("NLS") {
    constructor(command: String) : this(
        when (command[0]) {
            'C' -> Command.CursorInfo(command[1].digitToIntOrNull(), Command.CursorInfo.Type.fromChar(command[2]))
            'U' -> Command.Unicode(command[1].digitToInt(), Command.Property.fromChar(command[2]), command.substring(2))
            'A' -> Command.Ascii(command[1].digitToInt(), Command.Property.fromChar(command[2]), command.substring(2))
            else -> throw Exception("Unknown type")
        }
    )

    override fun toMessage() = command.cmd

    sealed class Command(val cmd: String) {
        data class SelectLine(val idx: Int) : Command("L$idx")
        data class SelectIndex(val idx: Int) : Command("I${idx.toString().padStart(5, '0')}")
        data class CursorInfo(val cursorPosition: Int?, val type: Type) : Command("C${cursorPosition ?: '-'}${type.encoding}") {
            enum class Type(val encoding: Char) {
                PageInformation('P'), PositionUpdate('C');

                companion object {
                    private val lookup = entries.associateBy { it.encoding }
                    fun fromChar(c: Char) = lookup[c] ?: PageInformation
                }
            }
        }
        data class Unicode(val line: Int, val property: Property, val text: String) : Command("U$line${property.encoding}$text")
        data class Ascii(val line: Int, val property: Property, val text: String) : Command("A$line${property.encoding}$text")

        enum class Property(val encoding: Char) {
            NO('-'), PLAYING('0'), ARTIST('A'), ALBUM('B'), FOLDER('F'), MUSIC('M'), PLAYLIST('P'),
            SEARCH('S'), ACCOUNT('a'), PLAYLIST_C('b'), STARRED('c'), UNSTARRED('d'), WHATS_NEW('e');

            companion object {
                private val lookup = entries.associateBy { it.encoding }
                fun fromChar(c: Char) = lookup[c] ?: NO
            }
        }
    }
}
