package no.nav.helse.flex.fss.proxy.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class PlainRestTemplateConfiguration {

    @Bean
    fun plainRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
    ): RestTemplate =
        restTemplateBuilder.build()
}
