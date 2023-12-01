package uk.co.thomasc.thealley.devicev2.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devicev2.AlleyDeviceMapper
import uk.co.thomasc.thealley.devicev2.IStateUpdater
import uk.co.thomasc.thealley.devicev2.kasa.bulb.BulbDevice
import uk.co.thomasc.thealley.devicev2.kasa.bulb.BulbState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Serializable
@SerialName("Bulb")
data class BulbConfig(
    override val name: String,
    override val host: String,
    val timeout: Duration = 10.minutes,
    val switchTimeout: Duration = 10.minutes,
    val sensors: List<Int> = listOf()
) : IAlleyConfig, IKasaConfig {
    override fun deviceConfig() = BulbDeviceConfig(this)

    class BulbDeviceConfig(val config: BulbConfig) : IAlleyDeviceConfig<BulbDevice, BulbConfig, BulbState>() {
        override fun create(id: Int, state: BulbState, stateStore: IStateUpdater<BulbState>, dev: AlleyDeviceMapper) = BulbDevice(id, config, state, stateStore)
        override fun stateSerializer() = BulbState.serializer()
    }
}
