package no.nav.helse.flex.fss.proxy.clientidvalidation

import no.nav.helse.flex.fss.proxy.config.ISSUER_AAD
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.helse.flex.fss.proxy.log
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException


object ClientIdValidation {

    private val log = log()

    fun Pair<TokenValidationContextHolder, List<String>>.validateClientId() {

        val clientIds = this.second

        val azp = this.first.hentAzpClaim()
        if (clientIds.ikkeInneholder(azp)) {
            throw JwtTokenUnauthorizedException("Ukjent client")
        }

    }

    private fun TokenValidationContextHolder.hentAzpClaim(): String {
        try {
            return this.tokenValidationContext.getJwtToken(ISSUER_AAD).jwtTokenClaims.getStringClaim("azp")!!
        } catch (e: Exception) {
            log.error("Fant ikke azp claim!", e)
            throw e
        }
    }

    private fun List<String>.ikkeInneholder(s: String): Boolean {
        return !this.contains(s)
    }
}
