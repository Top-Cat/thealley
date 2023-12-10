package uk.co.thomasc.thealley.google.followup

import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

object FollowUpAuth {
    private val jsonFile: String? = System.getenv("GOOGLE_CREDS")
    private val credentials = jsonFile?.let {
        GoogleCredentials.fromStream(FileInputStream(it))
            .createScoped(listOf("https://www.googleapis.com/auth/homegraph"))
    }

    fun canFollowUp(): Boolean {
        return credentials != null
    }

    fun getToken() =
        credentials?.let {
            it.refreshIfExpired()
            it.accessToken.tokenValue
        }
}
