package uk.co.thomasc.thealley

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.sockets.ServerSocket
import kotlinx.sockets.Socket
import kotlinx.sockets.aSocket
import org.springframework.stereotype.Component
import uk.co.thomasc.thealley.client.LocalClient
import uk.co.thomasc.thealley.devices.Bulb
import uk.co.thomasc.thealley.repo.SwitchConfig
import uk.co.thomasc.thealley.repo.SwitchRepository
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.time.LocalDateTime
import kotlin.experimental.and

@Component
class SwitchServer(val kasa: LocalClient, val switchRepo: SwitchRepository) {

    var server: ServerSocket =
        aSocket().tcp().bind(InetSocketAddress(5558))

    init {
        launch (CommonPool) {
            listenForClients()
        }.apply {
            invokeOnCompletion {
                server.close()
            }
        }
    }

    suspend fun listenForClients() {
        while (true) {
            val client = server.accept()
            launch(CommonPool) {
                client.use {
                    SwitchClient(kasa, switchRepo, it).run()
                }
            }
        }
    }

}

class SwitchClient(
    private val kasa: LocalClient,
    private val switchRepo: SwitchRepository,
    private val client: Socket) {

    private val buttonTimers = arrayOf<LocalDateTime?>(null, null)
    private var buttonState: Byte = 0

    private var cfg: SwitchConfig? = null

    enum class SwitchColorState(val state: Int, val flash: Boolean) {
        ON(1, false),
        FULL_FLASH(1, true),
        HALF(2, false),
        HALF_FLASH(2, true),
        OFF(0, false)
    }

    data class SwitchColor(
        val r: SwitchColorState,
        val g: SwitchColorState,
        val b: SwitchColorState
    ) {
        companion object {
            val RED = SwitchColor(SwitchColorState.ON, SwitchColorState.OFF, SwitchColorState.OFF)
            val GREEN = SwitchColor(SwitchColorState.OFF, SwitchColorState.ON, SwitchColorState.OFF)
            val BLUE = SwitchColor(SwitchColorState.OFF, SwitchColorState.OFF, SwitchColorState.ON)

            val YELLOW = SwitchColor(SwitchColorState.HALF, SwitchColorState.ON, SwitchColorState.OFF)
            val PINK = SwitchColor(SwitchColorState.HALF, SwitchColorState.OFF, SwitchColorState.ON)
            val TEAL = SwitchColor(SwitchColorState.OFF, SwitchColorState.ON, SwitchColorState.ON)

            val WHITE = SwitchColor(SwitchColorState.HALF, SwitchColorState.ON, SwitchColorState.ON)

            // Blue flashing
            val CONNECTED = BLUE.flash()
        }

        private fun mapToFlash(colorState: SwitchColorState) = when (colorState) {
            SwitchColorState.ON -> SwitchColorState.FULL_FLASH
            SwitchColorState.HALF -> SwitchColorState.HALF_FLASH
            else -> colorState
        }

        fun flash(): SwitchColor =
            this.copy(
                r = mapToFlash(r),
                g = mapToFlash(g),
                b = mapToFlash(b)
            )
    }

    private fun getPowerStateAnd(bulbA: (Bulb) -> (Bulb), bulbB: (Bulb) -> (Bulb)) {
        cfg?.let {
            val states = arrayOf(
                kasa.getDevice("lb130-${it.hostA}.guest.kirkstall.top-cat.me").bulb {
                    it?.let {
                        bulbA(it).getPowerState()
                    } ?: false
                },
                kasa.getDevice("lb130-${it.hostB}.guest.kirkstall.top-cat.me").bulb {
                    it?.let {
                        bulbB(it).getPowerState()
                    } ?: false
                }
            )

            runBlocking {
                val colors = states.map { if (it.await() == true) SwitchColor.GREEN else SwitchColor.RED }
                setColor(colors[0], colors[1])
            }
        }
    }

    private fun buttonDown(id: Int) {
        buttonTimers[id] = LocalDateTime.now()

        // TODO: button hold handling
        getPowerStateAnd(
            {
                when (id) {
                    0 -> it.togglePowerState()
                    else -> it
                }
            },
            {
                when (id) {
                    1 -> it.togglePowerState()
                    else -> it
                }
            }
        )
    }

    private fun buttonUp(id: Int) {
        buttonTimers[id] = null
    }

    suspend fun setColor(colorA: SwitchColor, colorB: SwitchColor) {
        // 0b000000 ON
        // 0b000000 HALF

        val inArr = arrayOf(colorA.r, colorA.g, colorA.b, colorB.r, colorB.g, colorB.b)//.reversed()
        val flashArr = inArr.map { if (it.flash) 1 else 0 }.reduceIndexed { index, acc, i -> (i shl index) or acc }
        val onArr = inArr.map { it.state and 0x01 }.reduceIndexed { index, acc, i -> (i shl index) or acc }
        val halfArr = inArr.map { (it.state and 0x02) shr 1 }.reduceIndexed { index, acc, i -> (i shl index) or acc }

        println("$onArr, $halfArr, $flashArr")

        val sendBuffer = ByteBuffer.allocate(4)
            .put(0).put(onArr.toByte()).put(halfArr.toByte()).put(flashArr.toByte())

        sendBuffer.flip()

        client.write(sendBuffer)
    }

    suspend fun run() {
        println("Client connected: ${client.remoteAddress}")
        val bb = ByteBuffer.allocate(32)

        while (true) {
            bb.clear()

            if (client.read(bb) == -1) {
                println("Client disconnected: ${client.remoteAddress}")
                return
            }

            bb.flip()

            while (bb.hasRemaining()) {
                when (bb.get().toInt()) {
                    0 -> {
                        val mac = arrayOf(bb.get(), bb.get(), bb.get())
                        val deviceMac = String.format("%02X:%02X:%02X", mac[0], mac[1], mac[2])
                        println("Switch init $deviceMac")

                        launch(CommonPool) {
                            cfg = switchRepo.getSwitchConfig(deviceMac)
                            getPowerStateAnd({ it }, { it })
                        }

                        setColor(SwitchColor.CONNECTED, SwitchColor.CONNECTED)
                    }
                    1 -> {
                        val button = bb.get()
                        println("Button state changed! ($button)")

                        arrayOf<Byte>(0x01, 0x02).forEachIndexed { index, it ->
                            if ((button and it) > 0 && (buttonState and it) < 1) {
                                buttonDown(index)
                            } else if ((button and it) < 1 && (buttonState and it) > 0) {
                                buttonUp(index)
                            }
                        }

                        buttonState = button
                    }
                }
            }
        }
    }

}
