package uk.co.thomasc.thealley.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import uk.co.thomasc.thealley.repo.DeviceType
import uk.co.thomasc.thealley.repo.SwitchRepository

@Controller
class UIController(val switchRepository: SwitchRepository) {

    @RequestMapping("/")
    fun home(model: Model): String {
        model.addAttribute("lights", switchRepository.getDevicesForType(DeviceType.BULB) +
            switchRepository.getDevicesForType(DeviceType.RELAY))
        return "home"
    }

}
