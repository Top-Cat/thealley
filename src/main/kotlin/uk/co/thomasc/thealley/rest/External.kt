package uk.co.thomasc.thealley.rest

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class GoogleHomeReq(val requestId: String, val inputs: JsonNode)

@RestController
@RequestMapping("/external")
class External {

    @PostMapping("/googlehome")
    fun googleHomeReq(@RequestBody obj: GoogleHomeReq) {
        println(obj.inputs)
    }

}
