package no.nav.helse.flex.fss.proxy.modiacontext

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.fss.proxy.clientidvalidation.ISSUER_AAD
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import javax.servlet.http.HttpServletResponse

@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class ModiacontextController(
    private val plainRestTemplate: RestTemplate,
    private val clientIdValidation: ClientIdValidation,
    @Value("\${modiacontextholder.url}") private val modiaContextHolderUrl: String,
) {
    @GetMapping(
        value = ["/modiacontextholder/api/context/aktivbruker"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun aktivBruker(
        requestEntity: RequestEntity<Any>,
    ): ResponseEntity<Any> {

        clientIdValidation.validateClientId(
            listOf(
                ClientIdValidation.NamespaceAndApp(namespace = "flex", app = "spinnsyn-frontend-interne")
            )
        )
        val headersInn = requestEntity.headers.toSingleValueMap()

        val headers = HttpHeaders()
        headers[HttpHeaders.COOKIE] = headersInn["cookie"]
        headers[HttpHeaders.AUTHORIZATION] = headersInn["xauthorization"]

        val req = HttpEntity<Void>(headers)
        return plainRestTemplate.exchange("$modiaContextHolderUrl/modiacontextholder/api/context/aktivbruker", HttpMethod.GET, req)
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
