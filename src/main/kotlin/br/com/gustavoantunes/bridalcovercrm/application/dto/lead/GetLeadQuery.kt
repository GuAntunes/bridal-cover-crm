package br.com.gustavoantunes.bridalcovercrm.application.dto.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId

/**
 * Query DTO for retrieving a lead by ID.
 * Used as input for the GetLeadUseCase.
 */
data class GetLeadQuery(
    val leadId: LeadId
)


