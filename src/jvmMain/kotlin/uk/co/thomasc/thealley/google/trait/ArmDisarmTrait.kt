package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import mu.KLogging
import uk.co.thomasc.thealley.alleyJson
import uk.co.thomasc.thealley.google.command.ArmDisarmCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

class ArmDisarmTrait(
    private val availableArmLevels: Set<ArmLevel>,
    private val ordered: Boolean,
    private val getArmState: suspend () -> State,
    private val arm: suspend (Boolean, String?) -> Unit
) : GoogleHomeTrait<ArmDisarmCommand>() {
    override val name = "action.devices.traits.ArmDisarm"
    override val klazz = ArmDisarmCommand::class

    @Serializable
    data class State(
        val isArmed: Boolean,
        val currentArmLevel: String? = null,
        val exitAllowance: Int? = null
    )

    override suspend fun getAttributes() = mapOf(
        "availableArmLevels" to JsonObject(
            mapOf(
                "levels" to alleyJson.encodeToJsonElement(availableArmLevels),
                "ordered" to JsonPrimitive(ordered)
            )
        )
    )

    override suspend fun getState() = alleyJson.encodeToJsonElement(getArmState()).jsonObject

    override suspend fun handleCommand(cmd: ArmDisarmCommand): ExecuteStatus {
        if (cmd.params.cancel != null) {
            return ExecuteStatus.ERROR(GoogleHomeErrorCode.AlreadyInState)
        }

        arm(cmd.params.arm, cmd.params.armLevel)

        return ExecuteStatus.SUCCESS()
        // return ExecuteStatus.ERROR(GoogleHomeErrorCode.ChallengeNeeded)
    }

    companion object : KLogging()
}
