package uk.co.thomasc.thealley.devices.onkyo

import mu.KLogging
import uk.co.thomasc.thealley.devices.onkyo.packet.ArtPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.AudioPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import uk.co.thomasc.thealley.devices.onkyo.packet.InputPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MasterVolumePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MutingPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetDeviceNamePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbAlbumNamePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbArtistNamePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbControlPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbListInfoPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbListTitleInfoPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbMenuStatusPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbPlayStatusPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbTimeInfoPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbTimeSeekPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbTitleNamePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.NetUsbTrackInfoPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.PowerPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.ReceiverInformationPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.VideoPacket
import java.nio.ByteBuffer
import java.nio.charset.Charset

class Packet(private val messageBytes: ByteArray) {
    constructor(content: String) : this(content.toByteArray())

    private fun header() = ByteBuffer.allocate(16)
        .put("ISCP".toByteArray())
        .putInt(16)
        .putInt(messageBytes.size)
        .put(1)
        .put(0)
        .put(0)
        .put(0)
        .array()

    fun bytes() = header() + messageBytes
    fun content() = messageBytes.toString(Charset.defaultCharset())

    fun typed(): IOnkyoResponse? {
        val str = messageBytes.toString(Charset.defaultCharset())
        val start = str.take(1)
        val direction = str.substring(1, 2)
        val group = str.substring(2, 5)
        val cmd = str.substring(5).removeSuffix("\u001A\r\n")

        return when (group) {
            "PWR" -> PowerPacket(cmd)
            "MVL" -> MasterVolumePacket(cmd)
            "AMT" -> MutingPacket(cmd)
            "IFA" -> AudioPacket(cmd)
            "SLI" -> InputPacket(cmd)
            "NJA" -> ArtPacket(cmd)
            "IFV" -> VideoPacket(cmd)
            "NRI" -> ReceiverInformationPacket(cmd)
            "NTC" -> NetUsbControlPacket(cmd)
            "NMS" -> NetUsbMenuStatusPacket(cmd)
            "NTM" -> NetUsbTimeInfoPacket(cmd)
            "NLS" -> NetUsbListInfoPacket(cmd)
            "NLT" -> NetUsbListTitleInfoPacket(cmd)
            "NST" -> NetUsbPlayStatusPacket(cmd)
            "NTI" -> NetUsbTitleNamePacket(cmd)
            "NAT" -> NetUsbArtistNamePacket(cmd)
            "NTR" -> NetUsbTrackInfoPacket(cmd)
            "NDN" -> NetDeviceNamePacket(cmd)
            "NAL" -> NetUsbAlbumNamePacket(cmd)
            "NTS" -> NetUsbTimeSeekPacket(cmd)
            else -> {
                logger.info { "Received unknown packet ${str.trim()}" }
                null
            }
        }
    }

    companion object : KLogging() {
        fun parse(bytes: ByteArray): Packet {
            val buffer = ByteBuffer.wrap(bytes)
            val magic = ByteArray(4)
            buffer.get(magic)

            magic.toString(Charset.defaultCharset()) == "ISCP" || throw OnkyoParseException("Bad magic")

            val headerLength = buffer.getInt()
            val contentLength = buffer.getInt()

            val version = buffer.get()
            version == 1.toByte() || throw OnkyoParseException("Bad version")
            buffer.position(headerLength)

            val message = ByteArray(contentLength)
            buffer.get(message)

            return Packet(message)
        }
    }
}
