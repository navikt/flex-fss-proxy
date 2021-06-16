package no.nav.helse.flex.fss.proxy.syfoservicestrangler

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
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SyfoservicestranglerController(
    private val clientIdValidation: ClientIdValidation,
    private val syfoserviceStranglerRestTemplate: RestTemplate,
    @Value("\${syfoservicestrangler.url}") private val syfoservicsstranglerUrl: String,
) {

    @PostMapping(
        "/api/syfoservicestrangler/brukeroppgave/soknad",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun opprettOppgave(@RequestBody req: OpprettHendelseRequest): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                NamespaceAndApp(namespace = "flex", app = "sykepengesoknad-narmesteleder-varsler")
            )
        )

        val forward: RequestEntity<Any> = RequestEntity(
            req,
            HttpHeaders(),
            HttpMethod.POST,
            URI("$syfoservicsstranglerUrl/api/brukeroppgave/soknad")
        )

        val responseEntity: ResponseEntity<Any> = syfoserviceStranglerRestTemplate.exchange(forward)

        val newHeaders: MultiValueMap<String, String> = LinkedMultiValueMap()
        responseEntity.headers.contentType?.let {
            newHeaders.set("Content-type", it.toString())
        }

        return responseEntity
    }

    data class OpprettHendelseRequest(
        val soknadId: String,
        val aktorId: String?,
        val orgnummer: String?,
        val type: String
    )

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
