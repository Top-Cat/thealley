package uk.co.thomasc.thealley.devices.onkyo

import kotlinx.coroutines.delay
import uk.co.thomasc.thealley.devices.onkyo.packet.ArtPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.AudioPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.IOnkyoResponse
import uk.co.thomasc.thealley.devices.onkyo.packet.InputPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MasterVolumePacket
import uk.co.thomasc.thealley.devices.onkyo.packet.MutingPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.PowerPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.ReceiverInformationPacket
import uk.co.thomasc.thealley.devices.onkyo.packet.VideoPacket

suspend fun getPowerStatus() {
    val queries = listOf(
        PowerPacket(),
        MasterVolumePacket(),
        ReceiverInformationPacket()
    )

    discovery(5000)?.firstOrNull()?.let { res ->
        println(res)

        OnkyoConnection(res.address.hostname).apply {
            init()

            queries.forEach { query ->
                when (val response = send<IOnkyoResponse>(query)) {
                    is MasterVolumePacket -> println("Volume is ${response.command}")
                    is PowerPacket -> println("Device is ${response.command}")
                    is MutingPacket -> println("Muted? ${response.command}")
                    is AudioPacket -> println("Audio? ${response.command}")
                    is VideoPacket -> println("Video? ${response.command}")
                    is ArtPacket -> println("Art? ${response.command}")
                    is InputPacket -> println("Input? ${response.command}")
                    is ReceiverInformationPacket -> println("XML! ${response.command}")
                    else -> println("Wat")
                }
            }

            delay(10000)

            send<MasterVolumePacket>(MasterVolumePacket(MasterVolumePacket.VolumeCommand.Level(72)))
            send<ReceiverInformationPacket>(ReceiverInformationPacket())
        }
    }
}
