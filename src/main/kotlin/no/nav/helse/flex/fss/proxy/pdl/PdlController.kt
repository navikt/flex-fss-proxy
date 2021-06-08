package no.nav.helse.flex.fss.proxy.pdl

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.NamespaceAndApp
import no.nav.helse.flex.fss.proxy.clientidvalidation.ISSUER_AAD
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class PdlController(
    private val clientIdValidation: ClientIdValidation,
    private val pdlRestTemplate: RestTemplate,
    @Value("\${pdl.url}") private val pdlUrl: String,
) {

    @PostMapping("/api/pdl/graphql", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun graphQl(requestEntity: RequestEntity<Any>): ResponseEntity<Any> {
        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "syfosoknad-kafka-replikerer"),
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-narmesteleder-varsler")
            )
        )

        val headers = requestEntity.headers.toSingleValueMap()

        val nyeHeaders = HttpHeaders()
        headers.forEach {
            if (it.key != AUTHORIZATION) {
                nyeHeaders.set(it.key, it.value)
            }
        }

        val forward: RequestEntity<Any> = RequestEntity(
            requestEntity.body,
            nyeHeaders,
            requestEntity.method,
            URI(pdlUrl)
        )

        val responseEntity: ResponseEntity<Any> = pdlRestTemplate.exchange(forward)

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
