package uk.co.thomasc.thealley.google.followup

import kotlinx.serialization.Serializable
import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

@Serializable
sealed interface IFollowUp {
    val status: Status

    enum class Status {
        SUCCESS, FAILURE
    }
}

data class FollowUpFailure(
    val errorCode: GoogleHomeErrorCode
) : IFollowUp {
    override val status = IFollowUp.Status.FAILURE
}

sealed class FollowUpSuccess : IFollowUp {
    override val status = IFollowUp.Status.SUCCESS
}

@Serializable
data class NetworkDownloadSpeedMbps(val networkDownloadSpeedMbps: Float) : FollowUpSuccess()

@Serializable
data class NetworkUploadSpeedMbps(val networkUploadSpeedMbps: Float) : FollowUpSuccess()

@Serializable
data class NetworkSpeedMbps(val networkDownloadSpeedMbps: Float, val networkUploadSpeedMbps: Float) : FollowUpSuccess()
