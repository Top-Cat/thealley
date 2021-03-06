package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import uk.co.thomasc.thealley.Config

enum class TadoMode(@JsonValue val mode: Int) {
    HOME(0),
    AWAY(1),
    UNKNOWN(2);

    companion object {
        @JsonCreator @JvmStatic fun fromString(str: String): TadoMode =
            values().firstOrNull { it.name == str } ?: UNKNOWN
    }
}

enum class TadoPower(@JsonValue val power: Int) {
    OFF(0),
    ON(1),
    UNKNOWN(2);

    companion object {
        @JsonCreator @JvmStatic fun fromString(str: String): TadoPower =
            TadoPower.values().firstOrNull { it.name == str } ?: TadoPower.UNKNOWN
    }
}

data class TokenResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String,
    val expires_in: Int,
    val scope: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZoneState(
    val tadoMode: TadoMode,
    val setting: JsonNode,
    val activityDataPoints: Map<String, JsonNode>,
    val sensorDataPoints: Map<String, JsonNode>
)

data class TransformedZoneState(
    val zone: Int,
    val tadoMode: TadoMode,
    val setting: TadoSetting?,
    val activityDataPoints: Map<String, TadoData?>,
    val sensorDataPoints: Map<String, TadoData?>
)

sealed class TadoData
sealed class TadoSetting

@JsonIgnoreProperties(ignoreUnknown = true)
data class PercentageData(
    val percentage: Float
): TadoData()

@JsonIgnoreProperties(ignoreUnknown = true)
data class TemperatureData(
    override val celsius: Float,
    override val fahrenheit: Float
): TadoTemperature, TadoData()

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
data class HeatingSetting(
    val power: TadoPower,
    val temperature: TemperatureSetting?
) : TadoSetting()

data class TemperatureSetting(
    override val celsius: Float,
    override val fahrenheit: Float
): TadoTemperature

interface TadoTemperature {
    val celsius: Float
    val fahrenheit: Float
}

val mapper = jacksonObjectMapper()
const val tadoApi = "https://my.tado.com"
const val tadoAuth = "https://auth.tado.com"

@Component
class TadoClient(val rest: RestTemplate, val config: Config) {

    var refreshToken = ""

    fun getToken() = if (refreshToken.isNotEmpty()) {
        refreshToken()
    } else {
        rest.postForObject(
                "$tadoAuth/oauth/token",
                HttpEntity(LinkedMultiValueMap(mapOf(
                        Pair("client_id", listOf("public-api-preview")),
                        Pair("client_secret", listOf("4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw")),
                        Pair("grant_type", listOf("password")),
                        Pair("scope", listOf("home.user")),
                        Pair("username", listOf("tado@thomasc.co.uk")),
                        Pair("password", listOf(config.tado.refreshToken))
                )),
                        HttpHeaders().apply {
                            contentType = MediaType.APPLICATION_FORM_URLENCODED
                        }
                ),
                TokenResponse::class.java
        )?.also { refreshToken = it.refresh_token }?.access_token ?: ""
    }

    fun refreshToken() = try {
        rest.postForObject(
                "$tadoAuth/oauth/token",
                HttpEntity(LinkedMultiValueMap(mapOf(
                        Pair("client_id", listOf("public-api-preview")),
                        Pair("client_secret", listOf("4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw")),
                        Pair("grant_type", listOf("refresh_token")),
                        Pair("scope", listOf("home.user")),
                        Pair("refresh_token", listOf(refreshToken))
                )),
                        HttpHeaders().apply {
                            contentType = MediaType.APPLICATION_FORM_URLENCODED
                        }
                ),
                TokenResponse::class.java
        )?.also { refreshToken = it.refresh_token }?.access_token ?: ""
    } catch (e: HttpClientErrorException) {
        // Force password refresh next time
        refreshToken = ""
    }

    fun getRawState(zone: Int): ZoneState? = rest.exchange(
            "$tadoApi/api/v2/homes/149676/zones/$zone/state",
            HttpMethod.GET,
            HttpEntity<ZoneState>(HttpHeaders().apply {
                add("Authorization", "Bearer ${getToken()}")
            }),
            ZoneState::class.java
        ).body

    fun getState() = (1..3).mapNotNull {
        zone -> getRawState(zone)?.let {
            TransformedZoneState(
                zone,
                it.tadoMode,
                when (it.setting.get("type").textValue()) {
                    "HEATING" -> mapper.treeToValue(it.setting, HeatingSetting::class.java)
                    else -> null
                },
                it.activityDataPoints.mapValues(this::mapDataPoints),
                it.sensorDataPoints.mapValues(this::mapDataPoints)
            )
        }
    }

    fun mapDataPoints(node: Map.Entry<String, JsonNode>) = when (node.value.get("type").textValue()) {
        "PERCENTAGE" -> mapper.treeToValue(node.value, PercentageData::class.java)
        "TEMPERATURE" -> mapper.treeToValue(node.value, TemperatureData::class.java)
        else -> null
    }

}
