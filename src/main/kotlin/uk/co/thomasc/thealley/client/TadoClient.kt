package uk.co.thomasc.thealley.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
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
            values().firstOrNull { it.name == str } ?: UNKNOWN
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
) : TadoData()

@JsonIgnoreProperties(ignoreUnknown = true)
data class TemperatureData(
    override val celsius: Float,
    override val fahrenheit: Float
) : TadoTemperature, TadoData()

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
data class HeatingSetting(
    val power: TadoPower,
    val temperature: TemperatureSetting?
) : TadoSetting()

data class TemperatureSetting(
    override val celsius: Float,
    override val fahrenheit: Float
) : TadoTemperature

interface TadoTemperature {
    val celsius: Float
    val fahrenheit: Float
}

const val tadoApi = "https://my.tado.com"
const val tadoAuth = "https://auth.tado.com"

class TadoClient(val config: Config) {

    var refreshToken = ""

    suspend fun getToken() = if (refreshToken.isNotEmpty()) {
        refreshToken()
    } else {
        client.post<TokenResponse>("$tadoAuth/oauth/token") {
            body = FormDataContent(
                Parameters.build {
                    append("client_id", "public-api-preview")
                    append("client_secret", "4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw")
                    append("grant_type", "password")
                    append("scope", "home.user")
                    append("username", "tado@thomasc.co.uk")
                    append("password", config.tado.refreshToken)
                }
            )
        }.also { refreshToken = it.refresh_token }.access_token
    }

    suspend fun refreshToken() = try {
        client.post<TokenResponse>("$tadoAuth/oauth/token") {
            body = FormDataContent(
                Parameters.build {
                    append("client_id", "public-api-preview")
                    append("client_secret", "4HJGRffVR8xb3XdEUQpjgZ1VplJi6Xgw")
                    append("grant_type", "refresh_token")
                    append("scope", "home.user")
                    append("refresh_token", refreshToken)
                }
            )
        }.also { refreshToken = it.refresh_token }.access_token
    } catch (e: Exception) {
        // Force password refresh next time
        refreshToken = ""
    }

    suspend fun getRawState(zone: Int) = try {
        client.get<ZoneState>("$tadoApi/api/v2/homes/149676/zones/$zone/state") {
            header(HttpHeaders.Authorization, "Bearer ${getToken()}")
        }
    } catch (e: Exception) { null }

    suspend fun getState() = (1..3).mapNotNull { zone ->
        getRawState(zone)?.let {
            TransformedZoneState(
                zone,
                it.tadoMode,
                when (it.setting.get("type").textValue()) {
                    "HEATING" -> jackson.treeToValue(it.setting, HeatingSetting::class.java)
                    else -> null
                },
                it.activityDataPoints.mapValues(this::mapDataPoints),
                it.sensorDataPoints.mapValues(this::mapDataPoints)
            )
        }
    }

    fun mapDataPoints(node: Map.Entry<String, JsonNode>) = when (node.value.get("type").textValue()) {
        "PERCENTAGE" -> jackson.treeToValue(node.value, PercentageData::class.java)
        "TEMPERATURE" -> jackson.treeToValue(node.value, TemperatureData::class.java)
        else -> null
    }
}
