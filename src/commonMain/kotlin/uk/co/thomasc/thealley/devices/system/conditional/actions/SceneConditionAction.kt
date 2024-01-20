package uk.co.thomasc.thealley.devices.system.conditional.actions

import kotlinx.serialization.Serializable

@Serializable
data class SceneConditionAction(val sceneId: Int) : IConditionAction
