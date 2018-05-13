package uk.co.thomasc.thealley.scenes

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.LocalDateTime
import java.util.Calendar

class Rule(
    private val sceneRepository: SceneRepository,

    val id: Int,
    val sensors: List<String>,
    private val timeout: Int,
    var lastActive: LocalDateTime,
    var offAt: LocalDateTime?,
    var lastUpdated: LocalDateTime,
    private val daytime: Boolean,
    private val scene: Scene
) {

    companion object {
        private val sunCalculator = SunriseSunsetCalculator(
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
                val sunrise = sunCalculator.getOfficialSunriseCalendarForDate(now)
                val sunset = sunCalculator.getOfficialSunsetCalendarForDate(now)

                sunsetCache = sunrise to sunset
                cacheTime = now

                sunsetCache
            }).let {
                it.first.before(now) && it.second.after(now)
            }
        }
    }

    fun onChange() {
        val now = LocalDateTime.now()

        // At night, turn on light and set off at time
        // Update last updated as state changes
        if (!(daytime && isDaytime())) {
            on(now, now.plusSeconds(timeout.toLong()))
        }

        // Always update last active time
        lastActive = now

        sceneRepository.updateLastActive(this)
    }

    fun tick() {
        val now = LocalDateTime.now()

        if (offAt?.let { it.isBefore(now) } == true) {
            // Turn off when offAt is reached, ignore null value
            scene.off()
            offAt = null
            lastUpdated = now

            sceneRepository.updateLastActive(this)
        } else if (!(daytime && isDaytime()) && lastUpdated.isBefore(lastActive) && lastActive.isBefore(now)) {
            // Not daytime, has been active since last updated, activity isn't in the future
            // -> Night has begun, recent movement triggers light

            val off = lastActive.plusSeconds(timeout.toLong())
            if (off.isBefore(now)) {
                offAt = null
                lastUpdated = now
            } else {
                on(now, off)
            }

            sceneRepository.updateLastActive(this)
        }
    }

    private fun on(now: LocalDateTime, off: LocalDateTime) {
        scene.execute()
        lastUpdated = now

        if (timeout > 0) {
            offAt = off
        }
    }

}
