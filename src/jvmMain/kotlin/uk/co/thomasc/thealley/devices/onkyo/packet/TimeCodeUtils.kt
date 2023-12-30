package uk.co.thomasc.thealley.devices.onkyo.packet

object TimeCodeUtils {
    fun toInt(code: String) =
        code.split(":").map { it.toIntOrNull() }.reduce { acc, s ->
            if (acc == null || s == null) {
                null
            } else {
                (acc * 60) + s
            }
        }

    fun fromInt(time: Int?, minimum: Int = 2): String =
        if (time == null) {
            (1..minimum).joinToString(":") { "--" }
        } else if (time < 60 && minimum <= 1) {
            time.toString().padStart(2, '0')
        } else {
            "${fromInt(time / 60, minimum - 1)}:${fromInt(time % 60, 1)}"
        }
}
