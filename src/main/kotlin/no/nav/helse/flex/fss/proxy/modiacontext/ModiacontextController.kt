package no.nav.helse.flex.fss.proxy.modiacontext

import no.nav.helse.flex.fss.proxy.logger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import javax.servlet.http.HttpServletResponse

@RestController
@Unprotected // TODO kan jeg få to scopes i et kall?
class ModiacontextController(
    private val plainRestTemplate: RestTemplate,
    @Value("\${modiacontextholder.url}") private val modiaContextHolderUrl: String,
) {
    val log = logger()

    @GetMapping(
        value = ["/modiacontextholder/api/context/aktivbruker"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun aktivBruker(
        requestEntity: RequestEntity<Any>,
    ): ResponseEntity<Any> {
        log.info("OK, jeg er inne i aktiv bruker")
        val headersInn = requestEntity.headers.toSingleValueMap()

        val headers = HttpHeaders()
        headers[HttpHeaders.COOKIE] = headersInn[HttpHeaders.COOKIE]
        headers[HttpHeaders.AUTHORIZATION] = headersInn["XAuthorization"]

        log.info("Proxyer med headere: $headers")
        val req = HttpEntity<Void>(headers)

        return plainRestTemplate.exchange("$modiaContextHolderUrl/modiacontextholder/api/context/aktivbruker", HttpMethod.GET, req)
    }

    @ExceptionHandler(HttpStatusCodeException::class)
    fun handleHttpStatusCodeException(response: HttpServletResponse, e: HttpStatusCodeException) {

        log.warn("Au, jeg er inne i errorhandlern! " + e.rawStatusCode, e)

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
