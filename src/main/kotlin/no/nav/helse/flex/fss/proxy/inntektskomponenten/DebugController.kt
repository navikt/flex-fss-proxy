package no.nav.helse.flex.fss.proxy.inntektskomponenten

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.util.*
import javax.servlet.http.HttpServletResponse

@RestController
class DebugController(
    private val inntektskomponentenRestTemplate: RestTemplate,
    @Value("\${INNTEKTSKOMPONENT_BASE_URL}") private val inntektskomponentenBaseUrl: String,
    private val clientIdValidation: ClientIdValidation,

) {

    @PostMapping(
        "/api/inntektskomponenten/api/v1/hentinntektliste/debug",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Unprotected
    fun hentInntektsliste(@RequestBody req: HentInntekterRequest): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                ClientIdValidation.NamespaceAndApp(
                    namespace = "flex",
                    app = "sykepengesoknad-andre-inntektskilder-logikk-test"
                )
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

        val responseEntity: ResponseEntity<Any> = inntektskomponentenRestTemplate.exchange(forward)

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
}
