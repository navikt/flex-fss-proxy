package no.nav.helse.flex.fss.proxy.clientidvalidation

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.fss.proxy.OBJECT_MAPPER
import no.nav.helse.flex.fss.proxy.logger
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

const val ISSUER_AAD = "aad"

@Component
class ClientIdValidation(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Value("\${AZURE_APP_PRE_AUTHORIZED_APPS}") private val azureAppPreAuthorizedApps: String
) {

    private val log = logger()
    private val allowedClientIds: List<PreAuthorizedClient> = OBJECT_MAPPER.readValue(azureAppPreAuthorizedApps)

    data class NamespaceAndApp(val namespace: String, val app: String)

    fun validateClientId(apps: List<NamespaceAndApp>) {
        val clientIds = allowedClientIds
            .filter { apps.contains(it.tilNamespaceAndApp()) }
            .map { it.clientId }

        val azp = tokenValidationContextHolder.hentAzpClaim()
        if (clientIds.ikkeInneholder(azp)) {
            throw UkjentClientException("Ukjent client")
        }
    }

    private fun TokenValidationContextHolder.hentAzpClaim(): String {
        try {
            return this.tokenValidationContext.getJwtToken(ISSUER_AAD).jwtTokenClaims.getStringClaim("azp")!!
        } catch (e: Exception) {
            log.error("Fant ikke azp claim!", e)
            throw UkjentClientException("ukjent feil", e)
        }
    }

    private fun List<String>.ikkeInneholder(s: String): Boolean {
        return !this.contains(s)
    }
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class UkjentClientException : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor(msg: String, cause: Throwable) : super(msg, cause)
}

private fun PreAuthorizedClient.tilNamespaceAndApp(): ClientIdValidation.NamespaceAndApp {
    val splitt = name.split(":")
    return ClientIdValidation.NamespaceAndApp(namespace = splitt[1], app = splitt[2])
}

data class PreAuthorizedClient(val name: String, val clientId: String)
