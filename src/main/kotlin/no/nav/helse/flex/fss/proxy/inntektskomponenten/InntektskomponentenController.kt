package no.nav.helse.flex.fss.proxy.inntektskomponenten

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.NamespaceAndApp
import no.nav.helse.flex.fss.proxy.clientidvalidation.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.logger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
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
    fun hentInntektsliste(@RequestBody req: JsonNode): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-backend"),
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-andre-inntektskilder-logikk-test")
            )
        )

        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = "srvflexfssproxy"
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        val forward: RequestEntity<Any> = RequestEntity(
            req,
            headers,
            HttpMethod.POST,
            URI("$inntektskomponentenBaseUrl/api/v1/hentinntektliste")
        )
        val start = Instant.now().toEpochMilli()

        val responseEntity: ResponseEntity<Any> = inntektskomponentenRestTemplate.exchange(forward)

        (Instant.now().toEpochMilli() - start).let {
            log.info("Kall til inntektskomponentenRestTemplate.exchange tok ${it.toInt()} millisekunder")
        }
        val newHeaders: MultiValueMap<String, String> = LinkedMultiValueMap()
        responseEntity.headers.contentType?.let {
            newHeaders.set("Content-type", it.toString())
        }

        return responseEntity
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

    data class Pong(val ping: Instant)

    @GetMapping(
        "/api/ping",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun ping(): Pong {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-backend"),
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-andre-inntektskilder-logikk-test")
            )
        )
        return Pong(Instant.now())
    }
}
