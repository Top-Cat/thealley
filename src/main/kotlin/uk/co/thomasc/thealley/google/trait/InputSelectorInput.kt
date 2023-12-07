package uk.co.thomasc.thealley.google.trait

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.google.GoogleHomeLang

@Serializable
data class InputSelectorInput(
    val key: String,
    val names: List<LocalizedName>
) {
    @Serializable
    data class LocalizedName(
        val lang: GoogleHomeLang,
        val nameSynonym: List<String>
    )
}
