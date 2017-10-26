package uk.co.thomasc.thealley.repo

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

data class SwitchConfig(
    val macAddr: String,
    val hostA: String,
    val hostB: String
)

@Component
class SwitchRepository(val db: JdbcTemplate) {

    fun getSwitchConfig(macAddr: String) =
        db.queryForObject(
            """
                |SELECT switch.macAddr, deviceA.hostname hostA, deviceB.hostname hostB
                |   FROM switch
                |   JOIN device deviceA
                |       ON switch.hostA = deviceA.id
                |   JOIN device deviceB
                |       ON switch.hostB = deviceB.id
                |   WHERE macAddr = ?
            """.trimMargin(),
            arrayOf(macAddr),
            this::switchConfigMapper
        )

    private fun switchConfigMapper(rs: ResultSet, row: Int) =
        SwitchConfig(
            rs.getString("macAddr"),
            rs.getString("hostA"),
            rs.getString("hostB")
        )
}
