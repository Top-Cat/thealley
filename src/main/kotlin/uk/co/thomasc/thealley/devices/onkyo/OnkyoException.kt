package uk.co.thomasc.thealley.devices.onkyo

open class OnkyoException(msg: String) : Exception(msg)

class OnkyoParseException(msg: String) : OnkyoException(msg)
