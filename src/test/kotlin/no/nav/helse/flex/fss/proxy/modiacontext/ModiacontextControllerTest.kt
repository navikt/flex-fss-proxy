package no.nav.helse.flex.fss.proxy.modiacontext

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
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.WebApplicationContext
import java.net.URI

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureMockMvc
class ModiacontextControllerTest {

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    private lateinit var server: MockOAuth2Server

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var plainRestTemplate: RestTemplate

    private lateinit var mockServer: MockRestServiceServer

    @BeforeEach
    fun init() {
        mockServer = MockRestServiceServer.createServer(plainRestTemplate)
    }

    @Test
    fun `ingen token returnerer 401`() {
        mockMvc.perform(
            get("/modiacontextholder/api/context/aktivbruker")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
        mockServer.verify()
    }

    @Test
    fun `riktig token returnerer 200`() {
        val cookie = "KAKEHEADER"
        val xauth = "Bearer ey12345"
        mockServer.expect(
            once(),
            requestTo(URI("http://modiacontexthodler/modiacontextholder/api/context/aktivbruker"))
        )
            .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
            .andExpect(header("cookie", cookie))
            .andExpect(header("authorization", xauth))
            .andRespond(
                MockRestResponseCreators.withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"hei\": 23}")
            )
        mockMvc.perform(
            get("/modiacontextholder/api/context/aktivbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token())
                .header("xauthorization", xauth)
                .header("cookie", cookie)

        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
    }

    @Test
    fun `feil client returnerer 403`() {
        mockMvc.perform(
            get("/modiacontextholder/api/context/aktivbruker")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token(clientId = "en-annen-client"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
        mockServer.verify()
    }

    private fun token(
        issuerId: String = "aad",
        clientId: String = "spinnsyn-frontend-interne-client-id",
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
