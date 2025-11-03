package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Email
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.Phone
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.GetLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.RegisterLeadUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

/**
 * Unit tests for LeadController.
 * 
 * Uses MockMvc to test HTTP endpoints without starting the full application.
 * Updated to use @MockitoBean instead of deprecated @MockBean.
 */
@WebMvcTest(LeadController::class)
class LeadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var registerLeadUseCase: RegisterLeadUseCase

    @MockitoBean
    private lateinit var getLeadUseCase: GetLeadUseCase

    @Test
    fun `should register lead successfully`() {
        // Given
        val leadId = LeadId.generate()
        val now = LocalDateTime.now()
        
        val mockLead = Lead(
            id = leadId,
            name = CompanyName("Vestidos Elegantes"),
            cnpj = null,
            contactInfo = ContactInfo(
                email = Email("contato@vestidoselegantes.com.br"),
                phone = Phone.fromBrazilianNumber("11987654321")
            ),
            status = LeadStatus.NEW,
            source = LeadSource.MANUAL_ENTRY,
            createdAt = now,
            updatedAt = now
        )

        whenever(registerLeadUseCase.registerLead(any())).thenReturn(mockLead)

        val requestBody = """
            {
                "companyName": "Vestidos Elegantes",
                "email": "contato@vestidoselegantes.com.br",
                "phone": "11987654321",
                "source": "MANUAL_ENTRY"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(leadId.toString()))
            .andExpect(jsonPath("$.companyName").value("Vestidos Elegantes"))
            .andExpect(jsonPath("$.contactInfo.email").value("contato@vestidoselegantes.com.br"))
            .andExpect(jsonPath("$.contactInfo.hasEmail").value(true))
            .andExpect(jsonPath("$.contactInfo.hasPhone").value(true))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.source").value("MANUAL_ENTRY"))
    }

    @Test
    fun `should return bad request when company name is missing`() {
        // Given
        val requestBody = """
            {
                "email": "contato@vestidoselegantes.com.br",
                "phone": "11987654321",
                "source": "MANUAL_ENTRY"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return bad request when source is invalid`() {
        // Given
        val requestBody = """
            {
                "companyName": "Vestidos Elegantes",
                "email": "contato@vestidoselegantes.com.br",
                "source": "INVALID_SOURCE"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/v1/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should get lead by id successfully`() {
        // Given
        val leadId = LeadId.generate()
        val now = LocalDateTime.now()
        
        val mockLead = Lead(
            id = leadId,
            name = CompanyName("Vestidos Elegantes"),
            cnpj = null,
            contactInfo = ContactInfo(
                email = Email("contato@vestidoselegantes.com.br"),
                phone = Phone.fromBrazilianNumber("11987654321")
            ),
            status = LeadStatus.NEW,
            source = LeadSource.MANUAL_ENTRY,
            createdAt = now,
            updatedAt = now
        )

        whenever(getLeadUseCase.getLeadById(any())).thenReturn(mockLead)

        // When & Then
        mockMvc.perform(
            get("/api/v1/leads/${leadId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(leadId.toString()))
            .andExpect(jsonPath("$.companyName").value("Vestidos Elegantes"))
            .andExpect(jsonPath("$.contactInfo.email").value("contato@vestidoselegantes.com.br"))
            .andExpect(jsonPath("$.status").value("NEW"))
            .andExpect(jsonPath("$.source").value("MANUAL_ENTRY"))
    }

    @Test
    fun `should return not found when lead does not exist`() {
        // Given
        val leadId = LeadId.generate()
        whenever(getLeadUseCase.getLeadById(any())).thenReturn(null)

        // When & Then
        mockMvc.perform(
            get("/api/v1/leads/${leadId}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return bad request when lead id is invalid`() {
        // When & Then
        mockMvc.perform(
            get("/api/v1/leads/invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }
}

