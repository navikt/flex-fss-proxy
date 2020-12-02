package no.nav.helse.flex.fss.proxy.config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation
@Configuration
class SecurityConfiguration(@Value("\${allowed.clientids}") private val clientIds: String) {


    @Bean
    fun allowedClientIds(): List<String> {
        return clientIds.split(",")
    }
}

const val ISSUER_AAD = "aad"
