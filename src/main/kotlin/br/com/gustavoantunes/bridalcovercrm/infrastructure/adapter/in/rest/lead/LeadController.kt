package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.UpdateLeadCommand
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadNotFoundException
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.DeleteLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.GetLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.ListLeadsUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.RegisterLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.UpdateLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead.LeadRequest
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead.LeadResponse
import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead.UpdateLeadRequest
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
    private val getLeadUseCase: GetLeadUseCase,
    private val updateLeadUseCase: UpdateLeadUseCase,
    private val deleteLeadUseCase: DeleteLeadUseCase,
    private val listLeadsUseCase: ListLeadsUseCase
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
            val lead = registerLeadUseCase.execute(command)
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
            val query = br.com.gustavoantunes.bridalcovercrm.application.dto.lead.GetLeadQuery(leadId)
            val lead = getLeadUseCase.execute(query)
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

    /**
     * Lists all leads with pagination support.
     * 
     * @param page Page number (0-based, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @return Paginated list of leads with HTTP status 200 (OK)
     */
    @GetMapping
    fun listLeads(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Map<String, Any>> {
        try {
            val pageResult = listLeadsUseCase.execute(page, size)
            val leadResponses = pageResult.content.map { LeadResponse.fromDomain(it) }
            
            val response = mapOf(
                "content" to leadResponses,
                "page" to pageResult.page,
                "size" to pageResult.size,
                "totalElements" to pageResult.totalElements,
                "totalPages" to pageResult.totalPages,
                "first" to pageResult.isFirst,
                "last" to pageResult.isLast
            )
            
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid pagination parameters: ${e.message}",
                e
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error listing leads: ${e.message}",
                e
            )
        }
    }

    /**
     * Updates an existing lead.
     * 
     * @param id Lead ID to be updated
     * @param request Updated lead data
     * @return The updated lead with HTTP status 200 (OK), or 404 (Not Found) if not exists
     */
    @PutMapping("/{id}")
    fun updateLead(
        @PathVariable id: String,
        @RequestBody request: UpdateLeadRequest
    ): ResponseEntity<LeadResponse> {
        try {
            val command = UpdateLeadCommand(
                id = id,
                companyName = request.companyName,
                cnpj = request.cnpj,
                email = request.email,
                phone = request.phone,
                instagram = request.instagram,
                status = request.status
            )
            
            val lead = updateLeadUseCase.execute(command)
            val response = LeadResponse.fromDomain(lead)
            
            return ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid request: ${e.message}",
                e
            )
        } catch (e: LeadNotFoundException) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                e.message,
                e
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating lead: ${e.message}",
                e
            )
        }
    }

    /**
     * Deletes a lead by its ID.
     * 
     * @param id Lead ID to be deleted
     * @return HTTP status 204 (No Content) on success, or 404 (Not Found) if not exists
     */
    @DeleteMapping("/{id}")
    fun deleteLead(@PathVariable id: String): ResponseEntity<Void> {
        try {
            deleteLeadUseCase.execute(id)
            return ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid lead ID: ${e.message}",
                e
            )
        } catch (e: LeadNotFoundException) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                e.message,
                e
            )
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error deleting lead: ${e.message}",
                e
            )
        }
    }
}
