package uk.co.thomasc.thealley.scenes

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import uk.co.thomasc.thealley.repo.SceneRepository
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.absoluteValue

class Rule(
    private val sceneRepository: SceneRepository,

    private val id: Int,
    val sensors: List<String>,
    private val timeout: Int,
    private var lastActive: LocalDateTime?,
    private var lastUpdated: LocalDateTime,
    private val daytime: Boolean,
    private val scene: Scene
) {

    companion object {
        val sunCalculator = SunriseSunsetCalculator(
            Location("53.8076891", "-1.5979767"),
            "Europe/London"
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
                val sunrise = sunCalculator.getCivilSunriseCalendarForDate(now)
                val sunset = sunCalculator.getCivilSunsetCalendarForDate(now)

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

        if (!(daytime && isDaytime())) {
            scene.execute()
            lastUpdated = now
        }

        if (timeout > 0) {
            lastActive = now
            sceneRepository.updateLastActive(id, lastActive, lastUpdated)
        }
    }

    fun tick() {
        if (lastActive == null) return

        val difference = Duration.between(LocalDateTime.now(), lastActive)
        val secondsSinceActivity = difference.seconds.absoluteValue

        if (secondsSinceActivity > timeout) {
            scene.off()

            lastActive = null
            sceneRepository.updateLastActive(id, lastActive, lastUpdated)
        } else if (!(daytime && isDaytime()) && lastUpdated.isBefore(lastActive)) {
            scene.execute()
            lastUpdated = LocalDateTime.now()
            sceneRepository.updateLastActive(id, lastActive, lastUpdated)
        }
    }

}
