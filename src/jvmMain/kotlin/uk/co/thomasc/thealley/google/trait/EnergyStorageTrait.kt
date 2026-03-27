package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.ChargeCommand
import uk.co.thomasc.thealley.google.command.IEnergyStorageCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class EnergyStorageTrait(
    private val queryOnlyEnergyStorage: Boolean = false,
    private val energyStorageDistanceUnitForUX: EnergyStorageDistanceUnit? = null,
    private val isRechargeable: Boolean? = null,
    private val getChargeState: suspend () -> State,
    private val setCharging: (suspend (Boolean) -> Unit)? = null,
    ) : GoogleHomeTrait<IEnergyStorageCommand<*>>() {
    enum class EnergyStorageDistanceUnit {
        KILOMETERS, MILES
    }

    enum class CapacityUnit {
        KILOMETERS, MILES, SECONDS, PERCENTAGE, KILOWATT_HOURS
    }

    enum class GoogleDescriptiveCapacity {
        CRITICALLY_LOW, LOW, MEDIUM, HIGH, FULL
    }

    @Serializable
    data class GoogleCapacity(
        val rawValue: Int,
        val unit: CapacityUnit
    )

    @Serializable
    data class State(
        val descriptiveCapacityRemaining: GoogleDescriptiveCapacity,
        val capacityRemaining: List<GoogleCapacity> = listOf(),
        val capacityUntilFull: List<GoogleCapacity> = listOf(),
        val isCharging: Boolean? = null,
        val isPluggedIn: Boolean? = null
    )

    override val name = "action.devices.traits.EnergyStorage"
    override val klazz = IEnergyStorageCommand::class

    override suspend fun getAttributes() = mapOf(
        "queryOnlyEnergyStorage" to JsonPrimitive(queryOnlyEnergyStorage),
        "energyStorageDistanceUnitForUX" to JsonPrimitive(energyStorageDistanceUnitForUX.toString()),
        "isRechargeable" to JsonPrimitive(isRechargeable)
    )

    override suspend fun getState() = alleyJson.encodeToJsonElement(getChargeState()).jsonObject

    override suspend fun handleCommand(cmd: IEnergyStorageCommand<*>): ExecuteStatus {
        when (cmd) {
            is ChargeCommand -> setCharging?.invoke(cmd.params.charge)
        }

        return ExecuteStatus.SUCCESS()
    }
}
