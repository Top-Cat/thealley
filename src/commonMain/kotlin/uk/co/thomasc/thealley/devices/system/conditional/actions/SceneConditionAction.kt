package uk.co.thomasc.thealley.devices.system.conditional.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Scene")
data class SceneConditionAction(val sceneId: Int) : IConditionAction
