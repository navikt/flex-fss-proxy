package no.nav.helse.flex.fss.proxy.syketilfelle

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.validateClientId
import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.config.PreAuthorizedClient
import no.nav.helse.flex.fss.proxy.token.TokenConsumer
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
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

    @PostMapping(
        value = ["reisetilskudd/{aktorId}/oppfolgingstilfelle"],
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun oppfolgingstilfelle(
        requestEntity: RequestEntity<Any>,
        @PathVariable aktorId: String
    ): ResponseEntity<Any> {
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
            .pathSegment("reisetilskudd", aktorId, "oppfolgingstilfelle")

        val forward: RequestEntity<Any> = RequestEntity(
            requestEntity.body,
            nyeHeaders,
            HttpMethod.POST,
            queryBuilder.build().toUri()
        )

        val responseEntity: ResponseEntity<Any> = syketilfelleRestTemplate.exchange(forward)

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
