package br.com.gustavoantunes.bridalcovercrm.application.dto.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo

/**
 * Command DTO for registering a new lead.
 * Used as input for the RegisterLeadUseCase.
 */
data class RegisterLeadCommand(
    val companyName: CompanyName,
    val cnpj: CNPJ? = null,
    val contactInfo: ContactInfo,
    val source: LeadSource
)


