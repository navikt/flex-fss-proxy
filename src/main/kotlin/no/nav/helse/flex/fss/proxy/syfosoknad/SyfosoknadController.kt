package no.nav.helse.flex.fss.proxy.syfosoknad

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.NamespaceAndApp
import no.nav.helse.flex.fss.proxy.clientidvalidation.ISSUER_AAD
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SyfosoknadController(
    private val clientIdValidation: ClientIdValidation,
    private val syfosoknadRestTemplate: RestTemplate,
    @Value("\${syfosoknad.url}") private val syfosoknadUrl: String,
) {

    @GetMapping(
        value = ["/syfosoknad/api/v3/soknader/{id}/kafkaformat"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun soknadKafkaformatV3(
        requestEntity: RequestEntity<Any>,
        @PathVariable id: String
    ): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-korrigering-metrikk"),
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-arkivering-oppgave")
            )
        )

        val headers = requestEntity.headers.toSingleValueMap()

        val nyeHeaders = HttpHeaders()
        headers.forEach {
            if (it.key != HttpHeaders.AUTHORIZATION) {
                nyeHeaders.set(it.key, it.value)
            }
        }

        val queryBuilder = UriComponentsBuilder
            .fromHttpUrl(syfosoknadUrl)
            .pathSegment("api", "v3", "soknader", id, "kafkaformat")

        val forward: RequestEntity<Any> = RequestEntity(
            requestEntity.body,
            nyeHeaders,
            HttpMethod.GET,
            queryBuilder.build().toUri()
        )

        val responseEntity: ResponseEntity<Any> = syfosoknadRestTemplate.exchange(forward)

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
