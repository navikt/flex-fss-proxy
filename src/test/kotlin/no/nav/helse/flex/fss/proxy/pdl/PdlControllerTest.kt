package no.nav.helse.flex.fss.proxy.pdl

import no.nav.helse.flex.fss.proxy.Application
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext


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


    @Test
    fun `ingen token returnerer 401`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andReturn()
    }

    @Test
    fun `riktig token returnerer 200`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()
    }

    @Test
    fun `feil client returnerer 403`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/pdl/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token(clientId = "en-annen-client")))
                .andExpect(MockMvcResultMatchers.status().isForbidden)
                .andReturn()
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
