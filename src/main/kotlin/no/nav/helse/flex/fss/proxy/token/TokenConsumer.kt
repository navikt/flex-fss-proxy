package no.nav.helse.flex.fss.proxy.token

import no.nav.helse.flex.fss.proxy.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant

@Component
class TokenConsumer(
    private val basicAuthRestTemplate: RestTemplate,
    @Value("\${security-token-service-token.url}") private val url: String
) {
    val log = log()
    @Volatile
    private var stsToken: Token? = null
    @Volatile
    private var utlopstidspunkt: Instant? = null

    val token: String
        @Retryable
        get() {
            val omToMinutter = Instant.now().plusSeconds(120L)
            return synchronized(this) {
                (
                    stsToken
                        ?.takeUnless { utlopstidspunkt?.isBefore(omToMinutter) ?: true }
                        ?: run {
                            log.info("Henter nytt token fra STS")
                            val headers = HttpHeaders()
                            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

                            val uriString = UriComponentsBuilder.fromHttpUrl(url)
                                .queryParam("grant_type", "client_credentials")
                                .queryParam("scope", "openid")
                                .toUriString()

                            val result =
                                basicAuthRestTemplate.exchange(uriString, GET, HttpEntity<Any>(headers), Token::class.java)

                            if (result.statusCode != OK) {
                                throw RuntimeException("Henting av token feiler med HTTP-" + result.statusCode)
                            }
                            val hentetToken: Token = result.body
                                ?: throw RuntimeException("Token hentet fra STS er tomt")
                            stsToken = hentetToken
                            utlopstidspunkt = Instant.now().plusSeconds(hentetToken.expires_in.toLong())
                            return@run hentetToken
                        }
                    ).access_token
            }
        }
}

data class Token(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
)
