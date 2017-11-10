package uk.co.thomasc.thealley

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class TheBinApplication {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()
}

fun main(args: Array<String>) {
    SpringApplication.run(TheBinApplication::class.java, *args)
}
//curl -X PUT -H "Accept: application/json" http://10.48.1.25/api/relay/0 --data "apikey=612E36334A7A3CD6&value=0"
//952fBMvrZejy3Fjw
