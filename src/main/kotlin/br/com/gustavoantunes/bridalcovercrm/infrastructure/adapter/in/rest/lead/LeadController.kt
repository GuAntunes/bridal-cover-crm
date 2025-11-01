package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.lead

import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.GetLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.RegisterLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead.LeadRequest
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead.LeadResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * REST Controller for Lead operations.
 * 
 * This is an input adapter (Hexagonal Architecture) that translates
 * HTTP requests into domain use case calls.
 */
@RestController
@RequestMapping("/api/v1/leads")
class LeadController(
    private val registerLeadUseCase: RegisterLeadUseCase,
    private val getLeadUseCase: GetLeadUseCase
) {

    /**
     * Registers a new lead in the system.
     * 
     * @param request Lead data to be registered
     * @return The registered lead with HTTP status 201 (Created)
     */
    @PostMapping
    fun registerLead(@RequestBody request: LeadRequest): ResponseEntity<LeadResponse> {
        try {
            val command = request.toCommand()
            val lead = registerLeadUseCase.registerLead(command)
            val response = LeadResponse.fromDomain(lead)
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid request: ${e.message}",
                e
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error registering lead: ${e.message}",
                e
            )
        }
    }

    /**
     * Retrieves a lead by its ID.
     * 
     * @param id Lead ID to be retrieved
     * @return The lead data with HTTP status 200 (OK), or 404 (Not Found) if not exists
     */
    @GetMapping("/{id}")
    fun getLeadById(@PathVariable id: String): ResponseEntity<LeadResponse> {
        try {
            val leadId = br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId.fromString(id)
            val lead = getLeadUseCase.getLeadById(leadId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Lead not found with ID: $id"
                )
            
            val response = LeadResponse.fromDomain(lead)
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid lead ID: ${e.message}",
                e
            )
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving lead: ${e.message}",
                e
            )
        }
    }
}
