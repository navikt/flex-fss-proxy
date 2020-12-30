package no.nav.helse.flex.fss.proxy.selvtest

import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class StsTestController(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    @GetMapping("/sts/test", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun isAlive(): OAuth2AccessTokenResponse? {
        val clientProperties = clientConfigurationProperties.registration["rest-sts"]
            ?: throw RuntimeException("Fant ikke config for rest-sts")

        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)

        return response
    }
}
