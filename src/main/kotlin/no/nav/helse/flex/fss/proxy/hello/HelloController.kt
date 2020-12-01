package no.nav.helse.flex.fss.proxy.hello

import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class HelloController(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    @GetMapping("/api/hello", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun isAlive(): ResponseEntity<String> {
        return  ResponseEntity.ok("HEI!")
    }
}
