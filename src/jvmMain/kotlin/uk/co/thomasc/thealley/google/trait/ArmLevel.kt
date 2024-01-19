package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.google.GoogleHomeLang

@Serializable
data class ArmLevel(
    @SerialName("level_name")
    val name: String,
    @SerialName("level_values")
    val values: Set<Value>
) {
    @Serializable
    data class Value(
        val lang: GoogleHomeLang,
        @SerialName("level_synonym")
        val synonym: List<String>
    )
}
