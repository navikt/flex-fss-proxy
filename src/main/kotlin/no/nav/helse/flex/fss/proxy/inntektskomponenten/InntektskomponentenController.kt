package no.nav.helse.flex.fss.proxy.inntektskomponenten

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.NamespaceAndApp
import no.nav.helse.flex.fss.proxy.clientidvalidation.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.logger
import no.nav.helse.flex.fss.proxy.serialisertTilString
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class InntektskomponentenController(
    private val clientIdValidation: ClientIdValidation,
    private val inntektskomponentenRestTemplate: RestTemplate,
    @Value("\${INNTEKTSKOMPONENT_BASE_URL}") private val inntektskomponentenBaseUrl: String,
) {
    val log = logger()

    @PostMapping(
        "/api/inntektskomponenten/api/v1/hentinntektliste",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ProtectedWithClaims(issuer = ISSUER_AAD)
    fun hentInntektsliste(@RequestBody req: HentInntekterRequest): HentInntekterResponse {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-backend")
            )
        )

        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = "srvflexfssproxy"
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val result = inntektskomponentenRestTemplate
            .exchange(
                URI("$inntektskomponentenBaseUrl/api/v1/hentinntektliste"),
                HttpMethod.POST,
                HttpEntity(
                    req.serialisertTilString(),
                    headers
                ),
                HentInntekterResponse::class.java
            )

        if (result.statusCode != HttpStatus.OK) {
            val message = "Kall mot inntektskomp feiler med HTTP-" + result.statusCode
            log.error(message)
            throw RuntimeException(message)
        }

        result.body?.let { return it }

        val message = "Kall mot inntektskomp returnerer ikke data"
        log.error(message)
        throw RuntimeException(message)
    }

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpStatusCodeException(response: HttpServletResponse, e: HttpStatusCodeException) {
        response.status = e.rawStatusCode
        if (e.responseHeaders != null) {
            val contentType = e.responseHeaders!!.contentType
            if (contentType != null) {
                response.contentType = contentType.toString()
            }
        }
        response.outputStream.write(e.responseBodyAsByteArray)
    }
}
