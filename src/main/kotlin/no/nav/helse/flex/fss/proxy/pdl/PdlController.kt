package no.nav.helse.flex.fss.proxy.pdl

import no.nav.helse.flex.fss.proxy.clientidvalidation.ClientIdValidation.validateClientId
import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.config.PreAuthorizedClient
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
@ProtectedWithClaims(issuer = ISSUER_AAD)
class PdlController(
        private val tokenValidationContextHolder: TokenValidationContextHolder,
        private val allowedClientIds: List<PreAuthorizedClient>,
) {

    @PostMapping("/api/pdl/graphql", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun graphQl(req: HttpServletRequest): ResponseEntity<String> {
        Pair(tokenValidationContextHolder, allowedClientIds).validateClientId()
        return ResponseEntity.ok("HEI!")
    }
}
