package uk.co.thomasc.thealley.devices

import uk.co.thomasc.thealley.web.google.GoogleHomeErrorCode

data class GetStateException(val errorCode: GoogleHomeErrorCode) : Exception("Error getting device state $errorCode")
