package no.nav.helse.flex.fss.proxy.inntektskomponenten

import no.nav.helse.flex.fss.proxy.logger
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Configuration
class InntektskomponentenRestTemplateConfiguration {

    val log = logger()

    @Bean
    fun inntektskomponentenRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        clientConfigurationProperties: ClientConfigurationProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate {
        val registrationName = "rest-sts"

        val clientProperties = clientConfigurationProperties.registration[registrationName]
            ?: throw RuntimeException("Fant ikke config for $registrationName")
        return restTemplateBuilder
            .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
            .build()
    }

    private fun bearerTokenInterceptor(
        clientProperties: ClientProperties,
        oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            request.headers.setBearerAuth(response.accessToken)
            val start = Instant.now().toEpochMilli()
            val execute = execution.execute(request, body)
            val slutt = Instant.now().toEpochMilli()
            val tid = slutt - start
            log.info("Kall til inntektskomponenten tok ${tid.toInt()} millisekunder")
            execute
        }
    }
}
