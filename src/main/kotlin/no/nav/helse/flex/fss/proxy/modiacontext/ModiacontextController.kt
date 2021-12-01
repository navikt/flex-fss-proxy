package no.nav.helse.flex.fss.proxy.modiacontext

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletResponse

@RestController
@Unprotected // TODO kan jeg få to scopes i et kall?
class ModiacontextController(
    private val plainRestTemplate: RestTemplate,
    @Value("\${modiacontextholder.url}") private val modiaContextHolderUrl: String,
) {

    @GetMapping(
        value = ["/modiacontextholder/api/context/aktivbruker"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun aktivBruker(
        requestEntity: RequestEntity<Any>,
    ): ResponseEntity<Any> {

        val headers = requestEntity.headers.toSingleValueMap()

        val nyeHeaders = HttpHeaders()
        headers.forEach {
            if (it.key == HttpHeaders.AUTHORIZATION || it.key == HttpHeaders.COOKIE) {
                nyeHeaders.set(it.key, it.value)
            }
        }

        val queryBuilder =
            UriComponentsBuilder.fromHttpUrl("$modiaContextHolderUrl/modiacontextholder/api/context/aktivbruker")

        val forward: RequestEntity<Any> = RequestEntity(
            requestEntity.body,
            nyeHeaders,
            HttpMethod.GET,
            queryBuilder.build().toUri()
        )

        return plainRestTemplate.exchange(forward)
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
