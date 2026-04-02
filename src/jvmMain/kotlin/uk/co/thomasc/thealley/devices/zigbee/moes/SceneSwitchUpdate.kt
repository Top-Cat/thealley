package uk.co.thomasc.thealley.devices.zigbee.moes

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.devices.zigbee.ZigbeeUpdateBattery

@Serializable
data class SceneSwitchUpdate(
    override val linkquality: Int,
    override val battery: Float?,

    val action: ZSceneAction? = null
) : ZigbeeUpdateBattery

enum class ZSceneAction(val button: Int, val times: Int) {
    @SerialName("1_single")
    A_ONCE(1, 1),

    @SerialName("1_double")
    A_DOUBLE(1, 2),

    @SerialName("2_single")
    B_ONCE(2, 1),

    @SerialName("2_double")
    B_DOUBLE(2, 2),

    @SerialName("3_single")
    C_ONCE(3, 1),

    @SerialName("3_double")
    C_DOUBLE(3, 2)
}
