package no.nav.helse.flex.fss.proxy.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun basicAuthRestTemplate(
        @Value("\${serviceuser.username}") username: String,
        @Value("\${serviceuser.password}") password: String,
    ): RestTemplate {
        return RestTemplateBuilder()
            .basicAuthentication(username, password)
            .build()
    }
}
