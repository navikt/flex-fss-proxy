package no.nav.helse.flex.fss.proxy.pdl

import no.nav.helse.flex.fss.proxy.Application
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
class PdlControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var pdlRestTemplate: RestTemplate

    private lateinit var mockServer: MockRestServiceServer

    @BeforeEach
    fun init() {
        mockServer = MockRestServiceServer.createServer(pdlRestTemplate)
    }

    @Test
    fun `ingen token returnerer 401`() {
        mockMvc.perform(
            post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
        mockServer.verify()
    }

    @Test
    fun `riktig token returnerer 200`() {
        mockServer.expect(
            once(),
            requestTo(URI("http://pdl"))
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"hei\": 23}")
            )
        mockMvc.perform(
            post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    fun `feil client returnerer 403`() {
        mockMvc.perform(
            post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token(clientId = "en-annen-client"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
        mockServer.verify()
    }

    private fun token(
        issuerId: String = "aad",
        clientId: String = "gsak-client-id",
        subject: String = "Samme det",
        audience: String = "flex-fss-proxy"
    ): String {
        return server.issueToken(
            issuerId,
            clientId,
            DefaultOAuth2TokenCallback(
                issuerId,
                subject,
                audience,
                emptyMap(),
                3600
            )
        ).serialize()
    }
}
