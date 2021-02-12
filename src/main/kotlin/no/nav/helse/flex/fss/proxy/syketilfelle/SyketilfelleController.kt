package no.nav.helse.flex.fss.proxy.syketilfelle

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.validateClientId
import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.config.PreAuthorizedClient
import no.nav.helse.flex.fss.proxy.log
import no.nav.helse.flex.fss.proxy.token.TokenConsumer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class SyketilfelleController(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    private val allowedClientIds: List<PreAuthorizedClient>,
    private val syketilfelleRestTemplate: RestTemplate,
    private val tokenConsumer: TokenConsumer,
    @Value("\${syketilfelle.url}") private val syketilfelleUrl: String,
) {
    private val log = log()

    @PostMapping(
        value = ["reisetilskudd/{aktorId}/{sykmeldingId}/erUtenforVentetid"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun erUtenforVentetid(
        requestEntity: RequestEntity<Any>,
        @PathVariable aktorId: String,
        @PathVariable sykmeldingId: String
    ): Boolean {
        Pair(tokenValidationContextHolder, allowedClientIds).validateClientId()

        val headers = requestEntity.headers.toSingleValueMap()

        val nyeHeaders = HttpHeaders()
        headers.forEach {
            if (it.key != AUTHORIZATION) {
                nyeHeaders.set(it.key, it.value)
            }
        }
        nyeHeaders.set("Authorization", "Bearer " + tokenConsumer.token)

        val queryBuilder = UriComponentsBuilder
            .fromHttpUrl(syketilfelleUrl)
            .pathSegment("reisetilskudd", aktorId, sykmeldingId, "erUtenforVentetid")

        val result = syketilfelleRestTemplate
            .exchange(
                queryBuilder.toUriString(),
                HttpMethod.POST,
                HttpEntity(requestEntity.body, nyeHeaders),
                Boolean::class.java
            )

        if (!result.statusCode.is2xxSuccessful) {
            val message = "Kall mot syfosyketilfelle feiler med HTTP-${result.statusCode}"
            log.error(message)
            throw RuntimeException(message)
        }

        return result.body ?: throw RuntimeException("Ingen data returnert fra syfosyketilfelle i erUtenforVentetid")
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
