package no.nav.helse.flex.fss.proxy.clientidvalidation

import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.helse.flex.fss.proxy.config.PreAuthorizedClient
import no.nav.helse.flex.fss.proxy.log
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


object ClientIdValidation {

    private val log = log()

    fun Pair<TokenValidationContextHolder, List<PreAuthorizedClient>>.validateClientId() {

        val clientIds = this.second.map { it.clientId }

        val azp = this.first.hentAzpClaim()
        if (clientIds.ikkeInneholder(azp)) {
            throw UkjentClientException("Ukjent client")
        }

    }

    private fun TokenValidationContextHolder.hentAzpClaim(): String {
        try {
            return this.tokenValidationContext.getJwtToken(ISSUER_AAD).jwtTokenClaims.getStringClaim("azp")!!
        } catch (e: Exception) {
            log.error("Fant ikke azp claim!", e)
            throw UkjentClientException(e)
        }
    }

    private fun List<String>.ikkeInneholder(s: String): Boolean {
        return !this.contains(s)
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class UkjentClientException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(cause: Throwable) : super(cause)
}

