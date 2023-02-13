package no.nav.helse.flex.fss.proxy.felleskodeverk

import jakarta.servlet.http.HttpServletResponse
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
import java.net.URI

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class FelleskodeverkController(
    private val clientIdValidation: ClientIdValidation,
    private val plainRestTemplate: RestTemplate,
    @Value("\${kodeverk.url}") private val kodeverkUrl: String,
) {

    @GetMapping(
        value = ["/api/v1/kodeverk/Krutkoder/koder/betydninger"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun krutkoder(requestEntity: RequestEntity<Any>): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "flex-joark-mottak")
            )
        )

        val headersInn = requestEntity.headers.toSingleValueMap()
        val nyeHeaders = HttpHeaders()

        headersInn.forEach {
            if (it.key != HttpHeaders.AUTHORIZATION) {
                nyeHeaders.set(it.key, it.value)
            }
        }

        val forward: RequestEntity<Any> = RequestEntity(
            nyeHeaders,
            HttpMethod.GET,
            URI("$kodeverkUrl/api/v1/kodeverk/Krutkoder/koder/betydninger?spraak=nb")
        )

        val responseEntity: ResponseEntity<Any> = plainRestTemplate.exchange(forward)

        val newHeaders: MultiValueMap<String, String> = LinkedMultiValueMap()
        responseEntity.headers.contentType?.let {
            newHeaders.set("Content-type", it.toString())
        }

        return responseEntity
    }

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpStatusCodeException(response: HttpServletResponse, e: HttpStatusCodeException) {
        response.status = e.statusCode.value()
        if (e.responseHeaders != null) {
            val contentType = e.responseHeaders!!.contentType
            if (contentType != null) {
                response.contentType = contentType.toString()
            }
        }
        response.outputStream.write(e.responseBodyAsByteArray)
    }
}
