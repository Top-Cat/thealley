package uk.co.thomasc.thealley

import org.apache.http.client.HttpClient
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableScheduling
class TheBinApplication {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
        builder.requestFactory {
            HttpComponentsClientHttpRequestFactory(httpClient())
        }.build()

    fun httpClient(): HttpClient = HttpClients.custom()
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setCookieSpec(CookieSpecs.STANDARD).build()
        ).build()
}

fun main(args: Array<String>) {
    SpringApplication.run(TheBinApplication::class.java, *args)
}