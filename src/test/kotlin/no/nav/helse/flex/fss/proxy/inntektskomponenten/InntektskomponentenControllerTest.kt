package no.nav.helse.flex.fss.proxy.inntektskomponenten

import no.nav.helse.flex.fss.proxy.Application
import no.nav.helse.flex.fss.proxy.serialisertTilString
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.WebApplicationContext
import java.net.URI

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureMockMvc
class InntektskomponentenControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var inntektskomponentenRestTemplate: RestTemplate

    private lateinit var mockServer: MockRestServiceServer
    val hentInntekterRequest = HentInntekterRequest(ident = Aktoer("sdf", "sdf"), ainntektsfilter = "8-28", "sdf", "juni", "august")

    @BeforeEach
    fun init() {
        mockServer = MockRestServiceServer.createServer(inntektskomponentenRestTemplate)
    }

    @Test
    fun `ingen token returnerer 401`() {
        mockMvc.perform(
            post("/api/inntektskomponenten/api/v1/hentinntektliste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(hentInntekterRequest.serialisertTilString())
                .content(hentInntekterRequest.serialisertTilString())

        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
        mockServer.verify()
    }

    @Test
    fun `riktig token returnerer 200`() {
        mockServer.expect(
            once(),
            requestTo(URI("http://inntektskomp/api/v1/hentinntektliste"))
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(HentInntekterResponse(ident = hentInntekterRequest.ident).serialisertTilString())
            )
        mockMvc.perform(
            post("/api/inntektskomponenten/api/v1/hentinntektliste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(hentInntekterRequest.serialisertTilString())
                .header("Authorization", "Bearer " + token())

        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    fun `feil client returnerer 403`() {
        mockMvc.perform(
            post("/api/inntektskomponenten/api/v1/hentinntektliste")
                .content(hentInntekterRequest.serialisertTilString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token(clientId = "en-annen-client"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
        mockServer.verify()
    }

    private fun token(
        issuerId: String = "aad",
        clientId: String = "flex-andre-inntektskilder-metrikker-client-id",
        subject: String = "Samme det",
        audience: List<String> = listOf("flex-fss-proxy")
    ): String {
        return server.issueToken(
            issuerId,
            clientId,
            DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                subject = subject,
                audience = audience,
                expiry = 3600
            )
        ).serialize()
    }
}
