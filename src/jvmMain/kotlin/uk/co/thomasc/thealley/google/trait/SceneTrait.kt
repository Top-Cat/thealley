package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import uk.co.thomasc.thealley.google.command.ActivateSceneCommand
import uk.co.thomasc.thealley.web.google.ExecuteStatus

class SceneTrait(
    private val sceneReversible: Boolean = false,
    private val executeScene: suspend (Boolean) -> Unit
) : GoogleHomeTrait<ActivateSceneCommand>() {
    override val name = "action.devices.traits.Scene"
    override val klazz = ActivateSceneCommand::class

    override suspend fun getAttributes() = mapOf(
        "sceneReversible" to JsonPrimitive(sceneReversible)
    )

    override suspend fun getState() = emptyMap<String, JsonElement>()

    override suspend fun handleCommand(cmd: ActivateSceneCommand): ExecuteStatus {
        executeScene(cmd.params.deactivate)

        return ExecuteStatus.SUCCESS()
    }
}
