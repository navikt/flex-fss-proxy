package no.nav.helse.flex.fss.proxy.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.fss.proxy.log
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableJwtTokenValidation
@Configuration
class SecurityConfiguration(@Value("\${AZURE_APP_PRE_AUTHORIZED_APPS}") private val azureAppPreAuthorizedApps: String) {

    private val log = log()

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private val list: List<PreAuthorizedClient>

    init {
        list = objectMapper.readValue(azureAppPreAuthorizedApps)
        log.info("Tillatter kall fra ${list.map { it.name }}")
    }

    @Bean
    fun preAuthorizedApps(): List<PreAuthorizedClient> {
        return list
    }
}

data class PreAuthorizedClient(val name: String, val clientId: String)

const val ISSUER_AAD = "aad"
