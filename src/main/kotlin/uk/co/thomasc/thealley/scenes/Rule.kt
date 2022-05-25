package uk.co.thomasc.thealley.scenes

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import uk.co.thomasc.thealley.repo.RuleTable
import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.min

data class RuleObj(val key: EntityID<Int>) : IntEntity(key) {
    companion object : IntEntityClass<RuleObj>(RuleTable)
    val lastActive by RuleTable.lastActive
    val scene by RuleTable.scene
    val timeout by RuleTable.timeout
    val lastUpdated by RuleTable.lastUpdated
    val daytime by RuleTable.daytime
    val offAt by RuleTable.offAt
}

data class Rule(private val sceneRepository: SceneRepository, val sensors: List<String>, val scene: Scene, val obj: RuleObj) {
    var lastActive = obj.lastActive
    var lastUpdated = obj.lastUpdated
    var offAt: LocalDateTime? = obj.offAt

    companion object {
        private val sunCalculator = SolarEventCalculator(
            Location("53.8076891", "-1.5979767"),
            "UTC"
        )

        lateinit var sunsetCache: Pair<Calendar, Calendar>
        var cacheTime: Calendar? = null

        private fun isDaytime(): Boolean {
            val now = Calendar.getInstance()

            return (cacheTime?.let {

                if (now.get(Calendar.YEAR) == it.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == it.get(Calendar.DAY_OF_YEAR)) {
                    sunsetCache
                } else {
                    null
                }
            } ?: run {
                val sunrise = sunCalculator.computeSunriseCalendar(Zenith(89.0), Calendar.getInstance())
                val sunset = sunCalculator.computeSunsetCalendar(Zenith(89.0), Calendar.getInstance())

                sunsetCache = sunrise to sunset
                cacheTime = now

                sunsetCache
            }).let {
                it.first.before(now) && it.second.after(now)
            }
        }

        const val overTime = 120
        const val minPercent = 20
    }

    fun onChange() {
        val now = LocalDateTime.now()

        // At night, turn on light and set off at time
        // Update last updated as state changes
        if (!(obj.daytime && isDaytime())) {
            on(now, now.plusSeconds(obj.timeout.toLong()))
        }

        // Always update last active time
        lastActive = now

        sceneRepository.updateLastActive(obj.id.value, this)
    }

    fun tick() {
        val now = LocalDateTime.now()

        if (offAt?.isBefore(now) == true) {
            // Turn off when offAt is reached, ignore null value
            scene.off()
            offAt = null
            lastUpdated = now

            sceneRepository.updateLastActive(obj.id.value, this)
        } else if (!(obj.daytime && isDaytime()) && lastUpdated.isBefore(lastActive) && lastActive.isBefore(now)) {
            // Not daytime, has been active since last updated, activity isn't in the future
            // -> Night has begun, recent movement triggers light

            val off = lastActive.plusSeconds(obj.timeout.toLong())
            if (off.isBefore(now)) {
                offAt = null
                lastUpdated = now
            } else {
                on(now, off)
            }

            sceneRepository.updateLastActive(obj.id.value, this)
        }
    }

    private fun getBrightnessFor(now: LocalDateTime): Int {
        val totalMinutes = if (now.hour < 2 || now.hour > 12) {
            // Count down to 2am
            val hoursUntilTwoAM = if (now.hour > 2) 25 - now.hour else 1 - now.hour

            (hoursUntilTwoAM * 60) + (60 - now.minute)
        } else if (now.hour > 4) {
            // Count up from 5am

            ((now.hour - 5) * 60) + now.minute
        } else {
            0
        }

        val scaled = (min(totalMinutes, overTime) * (100 - minPercent)) / overTime
        return minPercent + scaled
    }

    private fun on(now: LocalDateTime, off: LocalDateTime) {
        val percent = getBrightnessFor(now)

        scene.execute(percent)
        lastUpdated = now

        if (obj.timeout > 0) {
            offAt = off
        }
    }

}
